package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.PacMan.State.EMPOWERED;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.pacManDying;
import static de.amr.games.pacman.ui.Spritesheet.pacManWalking;

import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
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
import de.amr.games.pacman.model.Content;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Spritesheet;
import de.amr.statemachine.StateMachine;

public class PacMan extends MazeMover<PacMan.State> {

	private final StateMachine<State, GameEvent> sm;
	private ActiveEntityProvider activeEntityProvider;

	public PacMan(Game game, GameEventManager eventMgr, Tile home) {
		super(game, eventMgr, home, new EnumMap<>(State.class));
		sm = buildStateMachine();
		eventMgr = new GameEventManager("[Pac-Man]");
		activeEntityProvider = () -> Stream.empty();
		createSprites(2 * Spritesheet.TS);
		currentSprite = s_walking[Top4.E];
	}

	public void setActiveEntityProvider(ActiveEntityProvider activeEntityProvider) {
		this.activeEntityProvider = activeEntityProvider;
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

	private StateMachine<State, GameEvent> buildStateMachine() {
		/* @formatter:off */
		return StateMachine.builder(State.class, GameEvent.class).description("[Pac-Man]").initialState(State.INITIAL)

				.states()

				.state(State.INITIAL).onEntry(() -> {
					setMazePosition(homeTile);
					setDir(Top4.E);
					setNextDir(Top4.E);
					setSpeed(game::getPacManSpeed);
					getSprites().forEach(Sprite::resetAnimation);
					currentSprite = s_walking[getDir()];
				}).build()

				.state(State.DYING).duration(() -> game.sec(2)).onEntry(() -> {
					currentSprite = s_dying;
				}).build()

				.state(EMPOWERED).duration(game::getPacManEmpoweringTime).onEntry(() -> {
				}).onTick(() -> {
					walkAndInspectMaze();
					if (sm.getStateImpl().getRemaining() == sm.getStateImpl().getDuration() * 20 / 100) {
						eventMgr.publish(new PacManLosesPowerEvent());
					}
				}).build()

				.state(State.NORMAL).onTick(this::walkAndInspectMaze).build()

				.transitions()

				.change(State.INITIAL, State.NORMAL).build()

				.change(State.NORMAL, State.DYING).on(PacManKilledEvent.class).build()

				.change(State.NORMAL, State.EMPOWERED).on(PacManGainsPowerEvent.class).build()

				.keep(State.EMPOWERED).on(PacManGainsPowerEvent.class).build()

				.change(State.EMPOWERED, State.NORMAL).onTimeout()
				.act(t -> eventMgr.publish(new PacManLostPowerEvent())).build()

				.keep(State.DYING).onTimeout().act(t -> eventMgr.publish(new PacManDiedEvent())).build()

				.buildStateMachine();
		/* @formatter:on */
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
		inspectMaze();
	}

	private void inspectMaze() {
		Optional<GameEvent> find = activeEntityProvider.activeEntities().filter(this::collidesWith)
				.flatMap(this::description).findFirst();
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