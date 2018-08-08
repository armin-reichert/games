package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.PacMan.State.DYING;
import static de.amr.games.pacman.actor.PacMan.State.EMPOWERED;
import static de.amr.games.pacman.actor.PacMan.State.INITIAL;
import static de.amr.games.pacman.actor.PacMan.State.NORMAL;
import static de.amr.games.pacman.model.Content.isFood;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.core.GameEvent;
import de.amr.games.pacman.controller.event.core.GameEventManager;
import de.amr.games.pacman.controller.event.game.BonusFoundEvent;
import de.amr.games.pacman.controller.event.game.FoodFoundEvent;
import de.amr.games.pacman.controller.event.game.PacManDiedEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.game.PacManKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManLosesPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Spritesheet;
import de.amr.statemachine.StateMachine;

public class PacMan extends MazeMover<PacMan.State> {

	private final StateMachine<State, GameEvent> sm;
	private Environment environment;

	public PacMan(Game game, GameEventManager eventMgr) {
		super(game, eventMgr, game.maze.pacManHome, new EnumMap<>(State.class));
		sm = buildStateMachine();
		eventMgr = new GameEventManager("[Pac-Man]");
		environment = Environment.EMPTYNESS;
		createSprites(2*TS);
	}

	public void setEnvironment(Environment mazeLife) {
		this.environment = mazeLife;
	}

	// Pac-Man look

	private Sprite s_walking[] = new Sprite[4];
	private Sprite s_dying;
	private Sprite s_full;
	private Sprite currentSprite;

	private void createSprites(int size) {
		s_dying = Spritesheet.pacManDying().scale(size);
		s_full = Spritesheet.pacManFull().scale(size);
		TOPOLOGY.dirs().forEach(dir -> s_walking[dir] = Spritesheet.pacManWalking(dir).scale(size));
		currentSprite = s_walking[Top4.E];
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_walking), Stream.of(s_dying)).flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return currentSprite;
	}

	// Pac-Man behavior

	public enum State {
		INITIAL, NORMAL, EMPOWERED, DYING
	};

	@Override
	public StateMachine<State, GameEvent> getStateMachine() {
		return sm;
	}

	private StateMachine<State, GameEvent> buildStateMachine() {
		/* @formatter:off */
		return StateMachine.define(State.class, GameEvent.class)
				
			.description("[Pac-Man]")
			.initialState(INITIAL)

			.states()

				.state(INITIAL)
					.timeout(() ->game.sec(0.1f))
					.onEntry(() -> {
						setMazePosition(homeTile);
						setDir(Top4.E);
						setNextDir(Top4.E);
						setSpeed(game::getPacManSpeed);
						getSprites().forEach(Sprite::resetAnimation);
						currentSprite = s_full;
					})

				.state(DYING)
					.timeout(() -> game.sec(2))
					.onEntry(() -> {
						currentSprite = s_dying;
					})

				.state(EMPOWERED)
					.timeout(game::getPacManEmpoweringTime)
					.onTick(() -> {
						walkAndInspectMaze();
						if (sm.currentStateObject().getRemaining() == sm.currentStateObject().getDuration() * 20 / 100) {
							publishEvent(new PacManLosesPowerEvent());
						}
					})

				.state(NORMAL)
					.onTick(this::walkAndInspectMaze)

			.transitions()

				.when(INITIAL).become(NORMAL).onTimeout()

				.when(NORMAL).become(DYING)
					.on(PacManKilledEvent.class)

				.when(NORMAL).become(EMPOWERED)
					.on(PacManGainsPowerEvent.class)

				.when(EMPOWERED)
					.on(PacManGainsPowerEvent.class)
					.act(t -> sm.currentStateObject().resetTimer())

				.when(EMPOWERED).become(NORMAL)
					.onTimeout()
					.act(t -> publishEvent(new PacManLostPowerEvent()))

				.when(State.DYING)
					.onTimeout()
					.act(t -> publishEvent(new PacManDiedEvent()))

		.endStateMachine();
		/* @formatter:on */
	}

	@Override
	public void init() {
		sm.init();
	}
	
	// Pac-Man activities

	private void walkAndInspectMaze() {
		move();
		currentSprite = s_walking[getDir()];
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
			publishEvent(new PacManGhostCollisionEvent(collidingGhost.get()));
			return;
		}

		// Bonus discovered?
		Optional<Bonus> activeBonus = environment.activeBonus().filter(this::collidesWith);
		if (activeBonus.isPresent()) {
			Bonus bonus = activeBonus.get();
			publishEvent(new BonusFoundEvent(bonus.getSymbol(), bonus.getValue()));
			return;
		}

		// Food found on current tile?
		Tile tile = getTile();
		char content = maze.getContent(tile);
		if (isFood(content)) {
			publishEvent(new FoodFoundEvent(tile, content));
		}
	}
}