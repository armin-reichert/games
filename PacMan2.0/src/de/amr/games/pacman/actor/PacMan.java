package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.pacManDying;
import static de.amr.games.pacman.ui.Spritesheet.pacManWalking;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.core.GameEvent;
import de.amr.games.pacman.controller.event.game.BonusFoundEvent;
import de.amr.games.pacman.controller.event.game.FoodFoundEvent;
import de.amr.games.pacman.controller.event.game.PacManDiedEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.game.PacManKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManLosesPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Spritesheet;
import de.amr.statemachine.StateMachine;

public class PacMan extends MazeMover<PacMan.State> {

	private final StateMachine<State, GameEvent> sm;
	public final Set<GameEntity> interests = new HashSet<>();

	public PacMan(Game game, Maze maze, Tile home) {
		super(game, maze, home, new EnumMap<>(State.class));
		sm = new StateMachine<>(State.class);
		buildStateMachine(sm);
		createSprites(2 * Spritesheet.TS);
		currentSprite = s_walking[Top4.E]; // TODO
	}

	// Sprites

	private Sprite s_walking[] = new Sprite[4];
	private Sprite s_dying;
	private Sprite currentSprite;

	private void createSprites(int size) {
		s_dying = pacManDying().scale(size);
		TOPOLOGY.dirs().forEach(dir -> s_walking[dir] = pacManWalking(dir).scale(size));
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_walking), Stream.of(s_dying)).flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return currentSprite;
	}

	// state machine

	public enum State {
		INITIAL, NORMAL, EMPOWERED, DYING
	};

	@Override
	public StateMachine<State, GameEvent> getStateMachine() {
		return sm;
	}

	private void buildStateMachine(StateMachine<State, GameEvent> sm) {
		/*@formatter:off*/
		sm.builder()
			.description("Pac-Man")
			.initialState(State.INITIAL)

			.states()
				
				.state(State.INITIAL)
					.onEntry(state -> {
						setMazePosition(homeTile);
						setDir(Top4.E);
						setNextDir(Top4.E);
						setSpeed(game::getPacManSpeed);
						getSprites().forEach(Sprite::resetAnimation);
						currentSprite = s_walking[getDir()];
					})
				.build()
					
				.state(State.DYING)
					.onEntry(state -> {
						currentSprite = s_dying;
						state.setDuration(game.sec(1 + currentSprite.getAnimationSeconds()));
					})
				.build()
				
				.state(State.EMPOWERED)
					.onEntry(state -> {
						state.setDuration(game.getPacManEmpoweringTime());
					})
					.onTick(state -> {
						walkAndInspectMaze();
						if (state.getRemaining() == state.getDuration() * 20 / 100) {
							eventMgr.publish(new PacManLosesPowerEvent());
						}
					})
				.build()
				
				.state(State.NORMAL)
					.onTick(s -> walkAndInspectMaze())
				.build()
			
			.transitions()
				
				.change(State.INITIAL, State.NORMAL).build()
				
				.on(PacManKilledEvent.class)
					.change(State.NORMAL, State.DYING)
				.build()

				.on(PacManGainsPowerEvent.class)
					.change(State.NORMAL, State.EMPOWERED)
				.build()

				.on(PacManGainsPowerEvent.class)
					.keep(State.EMPOWERED)
				.build()
			
				.onTimeout()
					.act(t -> eventMgr.publish(new PacManLostPowerEvent()))
					.change(State.EMPOWERED, State.NORMAL)
				.build()

				.onTimeout()
					.act(t -> eventMgr.publish(new PacManDiedEvent()))
					.keep(State.DYING)
				.build()
					
		.buildStateMachine();
		/*@formatter:on*/
	}

	@Override
	public void init() {
		sm.init();
	}

	private void walkAndInspectMaze() {
		move();
		currentSprite = s_walking[getDir()];
		if (isTeleporting()) {
			return;
		}
		Optional<GameEvent> find = interests.stream().filter(this::collidesWith).flatMap(this::description).findFirst();
		if (find.isPresent()) {
			eventMgr.publish(find.get());
		} else {
			Tile tile = getTile();
			char content = maze.getContent(tile);
			if (Content.isFood(content)) {
				eventMgr.publish(new FoodFoundEvent(tile, content));
			}
		}
	}

	private Stream<GameEvent> description(GameEntity find) {
		if (find instanceof Ghost) {
			Ghost ghost = (Ghost) find;
			Ghost.State gs = ghost.getState();
			if (gs != Ghost.State.DEAD && gs != Ghost.State.DYING && gs != Ghost.State.SAFE) {
				return Stream.of(new PacManGhostCollisionEvent(ghost));
			}
		} else if (find instanceof Bonus) {
			Bonus bonus = (Bonus) find;
			if (!bonus.isHonored()) {
				return Stream.of(new BonusFoundEvent(bonus.getSymbol(), bonus.getValue()));
			}
		}
		return Stream.empty();
	}
}