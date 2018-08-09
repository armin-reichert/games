package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.PacMan.State.DYING;
import static de.amr.games.pacman.actor.PacMan.State.SAFE;
import static de.amr.games.pacman.actor.PacMan.State.STEROIDS;
import static de.amr.games.pacman.actor.PacMan.State.VULNERABLE;
import static de.amr.games.pacman.model.Content.isFood;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.GameUI.SPRITES;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.core.EventManager;
import de.amr.games.pacman.controller.event.game.BonusFoundEvent;
import de.amr.games.pacman.controller.event.game.FoodFoundEvent;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.controller.event.game.PacManDiedEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.game.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.game.PacManKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.StateMachine;

public class PacMan extends MazeMover<PacMan.State> {

	private final StateMachine<State, GameEvent> sm;
	private final EventManager<GameEvent> eventMgr;
	private Environment environment;

	public PacMan(Game game, EventManager<GameEvent> eventMgr) {
		super(game, game.maze.pacManHome, new EnumMap<>(State.class));
		sm = buildStateMachine();
		this.eventMgr = eventMgr;
		environment = Environment.EMPTYNESS;
		createSprites(2 * TS);
	}

	public void setEnvironment(Environment mazeLife) {
		this.environment = mazeLife;
	}

	// Pac-Man look

	private Sprite s_walking_to[] = new Sprite[4];
	private Sprite s_dying;
	private Sprite s_full;
	private Sprite s_current;

	private void createSprites(int size) {
		TOPOLOGY.dirs().forEach(dir -> s_walking_to[dir] = SPRITES.pacManWalking(dir).scale(size));
		s_dying = SPRITES.pacManDying().scale(size);
		s_full = SPRITES.pacManFull().scale(size);
		s_current = s_full;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_walking_to), Stream.of(s_dying)).flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return s_current;
	}

	// Pac-Man behavior

	public enum State {
		SAFE, VULNERABLE, STEROIDS, DYING
	};

	@Override
	public StateMachine<State, GameEvent> getStateMachine() {
		return sm;
	}

	private StateMachine<State, GameEvent> buildStateMachine() {
		/* @formatter:off */
		return StateMachine.define(State.class, GameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(SAFE)

			.states()

				.state(SAFE)
						.onEntry(() -> {
							setMazePosition(homeTile);
							setNextDir(Top4.E);
							setSpeed(game::getPacManSpeed);
							getSprites().forEach(Sprite::resetAnimation);
							s_current = s_full;
						})
						.timeout(() -> game.sec(0.1f))

				.state(VULNERABLE).onTick(this::walkAndInspectMaze)
					
				.state(STEROIDS)
						.onTick(() -> {
							walkAndInspectMaze();
							if (sm.getRemainingPct() == 20) { //TODO this can occur multiple times!
								eventMgr.publish(new PacManGettingWeakerEvent());
							}
						})
						.timeout(game::getPacManEmpoweringTime)

				.state(DYING)
						.onEntry(() -> s_current = s_dying)
						.timeout(() -> game.sec(2))

			.transitions()

					.when(SAFE).become(VULNERABLE).onTimeout()

					.when(VULNERABLE).become(DYING).on(PacManKilledEvent.class)
	
					.when(VULNERABLE).become(STEROIDS).on(PacManGainsPowerEvent.class)
	
					.when(STEROIDS).on(PacManGainsPowerEvent.class).act(e -> sm.resetTimer())
	
					.when(STEROIDS).become(VULNERABLE).onTimeout().act(e -> eventMgr.publish(new PacManLostPowerEvent()))
	
					.when(DYING).onTimeout().act(e -> eventMgr.publish(new PacManDiedEvent()))

		.endStateMachine();
		/* @formatter:on */
	}

	@Override
	public void init() {
		sm.init();
	}

	// Pac-Man activities

	@Override
	public void move() {
		super.move();
		s_current = s_walking_to[getDir()];
	}

	private void walkAndInspectMaze() {
		move();
		if (!isTeleporting()) {
			inspectMaze();
		}
	}

	private void inspectMaze() {
		// Ghost colliding?
		/*@formatter:off*/
		Optional<Ghost> collidingGhost = environment.activeGhosts()
				.filter(this::collidesWith)
				.filter(ghost -> ghost.getState() != Ghost.State.DEAD)
				.filter(ghost -> ghost.getState() != Ghost.State.DYING)
				.filter(ghost -> ghost.getState() != Ghost.State.SAFE)
				.findFirst();
		/*@formatter:on*/
		if (collidingGhost.isPresent()) {
			eventMgr.publish(new PacManGhostCollisionEvent(collidingGhost.get()));
			return;
		}

		// Bonus discovered?
		Optional<Bonus> activeBonus = environment.activeBonus().filter(this::collidesWith);
		if (activeBonus.isPresent()) {
			Bonus bonus = activeBonus.get();
			eventMgr.publish(new BonusFoundEvent(bonus.getSymbol(), bonus.getValue()));
			return;
		}

		// Food found on current tile?
		Tile tile = getTile();
		char content = maze.getContent(tile);
		if (isFood(content)) {
			eventMgr.publish(new FoodFoundEvent(tile, content));
		}
	}
}