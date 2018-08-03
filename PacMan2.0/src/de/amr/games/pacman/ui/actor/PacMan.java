package de.amr.games.pacman.ui.actor;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.getPacManDying;
import static de.amr.games.pacman.ui.Spritesheet.getPacManWalking;

import java.util.EnumMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGhostCollisionEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLosesPowerEvent;
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.model.TileContent;
import de.amr.games.pacman.ui.Spritesheet;
import de.amr.statemachine.StateMachine;

public class PacMan extends MazeMover<PacMan.State> {

	private final StateMachine<State, GameEvent> sm;
	private final Set<GameEntity> interests;

	public PacMan(Game game, Maze maze, Tile home, Set<GameEntity> interests) {
		super(game, maze, home, new EnumMap<>(State.class));
		this.interests = interests;
		sm = createStateMachine();
		createSprites();
		currentSprite = s_walking[Top4.E]; // TODO
	}

	// Sprites

	private Sprite s_walking[] = new Sprite[4];
	private Sprite s_dying;
	private Sprite currentSprite;

	private void createSprites() {
		int size = 2 * Spritesheet.TS;
		s_dying = new Sprite(getPacManDying()).scale(size).animation(AnimationMode.LINEAR, 100);
		TOPOLOGY.dirs().forEach(dir -> {
			s_walking[dir] = new Sprite(getPacManWalking(dir)).scale(size).animation(AnimationMode.BACK_AND_FORTH, 80);
		});
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

		sm.change(State.INITIAL, State.NORMAL);

		// NORMAL

		sm.state(State.NORMAL).entry = state -> {
			currentSprite = s_walking[getDir()];
		};

		sm.state(State.NORMAL).update = s -> {
			currentSprite = s_walking[getDir()];
			walkMaze();
		};

		sm.state(State.NORMAL).changeOnInput(PacManKilledEvent.class, State.DYING);

		sm.state(State.NORMAL).changeOnInput(PacManGainsPowerEvent.class, State.EMPOWERED);

		// EMPOWERED

		sm.state(State.EMPOWERED).entry = state -> {
			state.setDuration(game.getPacManEmpoweringTime());
		};

		sm.state(State.EMPOWERED).update = state -> {
			currentSprite = s_walking[getDir()];
			walkMaze();
			if (state.getRemaining() == state.getDuration() * 20 / 100) {
				eventMgr.publish(new PacManLosesPowerEvent(this));
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
		if (TileContent.isFood(content)) {
			eventMgr.publish(new FoodFoundEvent(tile, content));
			return;
		}
	}

	private GameEvent findInterestingThing() {
		for (GameEntity finding : interests.stream().filter(this::collidesWith).collect(Collectors.toSet())) {
			if (finding instanceof Ghost) {
				Ghost ghost = (Ghost) finding;
				if (ghost.getState() != Ghost.State.DEAD && ghost.getState() != Ghost.State.DYING) {
					return new PacManGhostCollisionEvent(this, ghost);
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