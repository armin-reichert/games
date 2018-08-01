package de.amr.games.pacman.ui.actor;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.getPacManDying;
import static de.amr.games.pacman.ui.Spritesheet.getPacManWalking;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.controller.event.BonusFoundEvent;
import de.amr.games.pacman.controller.event.FoodFoundEvent;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostContactEvent;
import de.amr.games.pacman.controller.event.PacManDiedEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManKilledEvent;
import de.amr.games.pacman.controller.event.PacManLosesPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.model.TileContent;
import de.amr.games.pacman.ui.Spritesheet;
import de.amr.statemachine.StateMachine;

public class PacMan extends MazeMover<PacMan.State> {

	public final Set<GameEntity> lookFor = new HashSet<>();

	public PacMan(Game game, Maze maze, Tile home) {
		super(game, maze, "Pac-Man", home, new EnumMap<>(State.class));
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
			s_walking[dir] = new Sprite(getPacManWalking(dir)).scale(size)
					.animation(AnimationMode.BACK_AND_FORTH, 80);
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
		NORMAL, EMPOWERED, DYING
	};

	@Override
	protected StateMachine<State, GameEvent> createStateMachine() {
		StateMachine<State, GameEvent> sm = new StateMachine<>("Pac-Man", State.class, State.NORMAL);

		// NORMAL

		sm.state(State.NORMAL).entry = state -> {
			currentSprite = s_walking[getDir()];
		};

		sm.state(State.NORMAL).update = s -> {
			currentSprite = s_walking[getDir()];
			walkAround();
		};

		sm.changeOnInput(PacManKilledEvent.class, State.NORMAL, State.DYING);

		sm.changeOnInput(PacManGainsPowerEvent.class, State.NORMAL, State.EMPOWERED, t -> {
			PacManGainsPowerEvent e = event(t);
			sm.state(State.EMPOWERED).setDuration(e.ticks);
		});

		// EMPOWERED

		sm.state(State.EMPOWERED).update = s -> {
			currentSprite = s_walking[getDir()];
			walkAround();
		};

		sm.changeOnTimeout(State.EMPOWERED, State.NORMAL,
				t -> eventMgr.publish(new PacManLosesPowerEvent(this)));

		// DYING

		sm.state(State.DYING).entry = state -> {
			state.setDuration(game.getPacManDyingTime());
			currentSprite = s_dying;
		};

		sm.changeOnTimeout(State.DYING, State.NORMAL, t -> {
			eventMgr.publish(new PacManDiedEvent());
		});

		return sm;
	}

	@Override
	public void init() {
		placeAt(maze.pacManHome);
		setDir(Top4.E);
		setNextDir(Top4.E);
		getSprites().forEach(Sprite::resetAnimation);
		setSpeed(game::getPacManSpeed);
		sm.init();
	}

	private void walkAround() {
		move();
		if (isOutsideMaze()) {
			return; // teleporting
		}
		Tile tile = getTile();
		char content = maze.getContent(tile);
		if (TileContent.isFood(content)) {
			eventMgr.publish(new FoodFoundEvent(tile, content));
		} else {
			lookFor.stream().filter(this::collidesWith).findAny().ifPresent(finding -> {
				if (finding instanceof Ghost) {
					Ghost ghost = (Ghost) finding;
					if (ghost.getState() != Ghost.State.DEAD && ghost.getState() != Ghost.State.DYING) {
						eventMgr.publish(new GhostContactEvent(this, ghost));
					}
				} else if (finding instanceof Bonus) {
					Bonus bonus = (Bonus) finding;
					eventMgr.publish(new BonusFoundEvent(bonus.getSymbol(), bonus.getValue()));
				}
			});
		}
	}
}