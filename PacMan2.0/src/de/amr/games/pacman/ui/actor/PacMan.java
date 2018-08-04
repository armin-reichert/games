package de.amr.games.pacman.ui.actor;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.pacManDying;
import static de.amr.games.pacman.ui.Spritesheet.pacManWalking;

import java.util.EnumMap;
import java.util.Set;
import java.util.stream.Collectors;
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
	private final Set<GameEntity> interests;

	public PacMan(Game game, Maze maze, Tile home, Set<GameEntity> interests) {
		super(game, maze, home, new EnumMap<>(State.class));
		this.interests = interests;
		sm = createStateMachine();
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

	protected StateMachine<State, GameEvent> createStateMachine() {
		StateMachine<State, GameEvent> sm = new StateMachine<>("Pac-Man", State.class, State.INITIAL);

		// INITIAL

		sm.state(State.INITIAL).entry = state -> {
			setMazePosition(homeTile);
			setDir(Top4.E);
			setNextDir(Top4.E);
			setSpeed(game::getPacManSpeed);
			getSprites().forEach(Sprite::resetAnimation);
			currentSprite = s_walking[getDir()];
		};

		sm.state(State.INITIAL).change(State.NORMAL);

		// NORMAL

		sm.state(State.NORMAL).update = s -> walkMaze();

		sm.state(State.NORMAL).changeOnInput(PacManKilledEvent.class, State.DYING);

		sm.state(State.NORMAL).changeOnInput(PacManGainsPowerEvent.class, State.EMPOWERED);

		// EMPOWERED

		sm.state(State.EMPOWERED).entry = state -> {
			state.setDuration(game.getPacManEmpoweringTime());
		};

		sm.state(State.EMPOWERED).update = state -> {
			walkMaze();
			if (state.getRemaining() == state.getDuration() * 20 / 100) {
				eventMgr.publish(new PacManLosesPowerEvent());
			}
		};

		sm.state(State.EMPOWERED).changeOnTimeout(State.NORMAL, t -> eventMgr.publish(new PacManLostPowerEvent()));

		// DYING

		sm.state(State.DYING).entry = state -> {
			state.setDuration(game.getPacManDyingTime());
			currentSprite = s_dying;
		};

		sm.state(State.DYING).onTimeout(t -> eventMgr.publish(new PacManDiedEvent()));

		return sm;
	}

	@Override
	public void init() {
		sm.init();
	}

	private void walkMaze() {
		move();
		currentSprite = s_walking[getDir()];
		if (isTeleporting()) {
			return;
		}
		GameEvent finding = findInterestingThing();
		if (finding != null) {
			eventMgr.publish(finding);
			return;
		}
		Tile tile = getTile();
		char content = maze.getContent(tile);
		if (Content.isFood(content)) {
			eventMgr.publish(new FoodFoundEvent(tile, content));
			return;
		}
	}

	private GameEvent findInterestingThing() {
		for (GameEntity finding : interests.stream().filter(this::collidesWith).collect(Collectors.toSet())) {
			if (finding instanceof Ghost) {
				Ghost ghost = (Ghost) finding;
				if (ghost.getState() != Ghost.State.DEAD && ghost.getState() != Ghost.State.DYING) {
					return new PacManGhostCollisionEvent(ghost);
				}
			} else if (finding instanceof Bonus) {
				Bonus bonus = (Bonus) finding;
				if (!bonus.isHonored()) {
					return new BonusFoundEvent(bonus.getSymbol(), bonus.getValue());
				}
			}
		}
		return null;
	}
}