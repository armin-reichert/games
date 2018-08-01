package de.amr.games.pacman.ui.actor;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.getGhostBlue;
import static de.amr.games.pacman.ui.Spritesheet.getGhostBlueWhite;
import static de.amr.games.pacman.ui.Spritesheet.getGhostEyes;
import static de.amr.games.pacman.ui.Spritesheet.getGhostNormal;
import static de.amr.games.pacman.ui.Spritesheet.getGreenNumber;

import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GhostKilledEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManLosesPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Spritesheet;
import de.amr.statemachine.StateMachine;

public class Ghost extends MazeMover<Ghost.State> {

	private final int color;

	public Ghost(Game game, Maze maze, String name, int color, Tile home) {
		super(game, maze, name, home, new EnumMap<>(State.class));
		this.color = color;
		createSprites(color);
		createStateMachine();
	}

	public int getColor() {
		return color;
	}

	// Sprites

	private Sprite s_normal[] = new Sprite[4];
	private Sprite s_dying[] = new Sprite[4];
	private Sprite s_dead[] = new Sprite[4];
	private Sprite s_afraid;
	private Sprite s_brave;
	private Sprite s_points;

	private void createSprites(int color) {
		int size = 2 * Spritesheet.TS;
		TOPOLOGY.dirs().forEach(dir -> {
			s_normal[dir] = new Sprite(getGhostNormal(color, dir)).scale(size)
					.animation(AnimationMode.BACK_AND_FORTH, 300);
			s_dead[dir] = new Sprite(getGhostEyes(dir)).scale(size);
		});
		for (int i = 0; i < 4; ++i) {
			s_dying[i] = new Sprite(getGreenNumber(i)).scale(size);
		}
		s_points = s_dying[0];
		s_afraid = new Sprite(getGhostBlue()).scale(size).animation(AnimationMode.CYCLIC, 200);
		s_brave = new Sprite(getGhostBlueWhite()).scale(size).animation(AnimationMode.CYCLIC, 100);
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_normal), Stream.of(s_dying), Stream.of(s_dead),
				Stream.of(s_afraid, s_brave, s_points)).flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		switch (getState()) {
		case AGGRO:
		case SAFE:
		case SCATTERING:
			return s_normal[getDir()];
		case DYING:
			return s_points;
		case DEAD:
			return s_dead[getDir()];
		case AFRAID:
			return s_afraid;
		case BRAVE:
			return s_brave;
		default:
			throw new IllegalStateException("Illegal ghost state: " + getState());
		}
	}

	// State machine

	public enum State {
		AGGRO, SCATTERING, AFRAID, BRAVE, DYING, DEAD, SAFE
	}

	@Override
	protected StateMachine<State, GameEvent> createStateMachine() {
		StateMachine<State, GameEvent> sm = new StateMachine<>(getName(), State.class, State.SAFE);

		// SAFE

		sm.state(State.SAFE).entry = state -> {
			state.setDuration(game.sec(3));
		};

		sm.state(State.SAFE).update = state -> move();

		sm.changeOnTimeout(State.SAFE, State.AGGRO);
		sm.changeOnInput(PacManLosesPowerEvent.class, State.SAFE, State.AGGRO);

		// AGGRO
		sm.state(State.AGGRO).update = state -> move();

		sm.changeOnInput(PacManGainsPowerEvent.class, State.AGGRO, State.AFRAID);

		// AFRAID

		sm.state(State.AFRAID).update = state -> move();

		sm.change(State.AFRAID, State.SAFE, () -> getTile().equals(homeTile));

		sm.changeOnInput(PacManLosesPowerEvent.class, State.AFRAID, State.AGGRO);

		sm.changeOnInput(GhostKilledEvent.class, State.AFRAID, State.DYING);

		// BRAVE

		sm.state(State.BRAVE).update = state -> move();

		// DYING

		sm.state(State.DYING).entry = state -> {
			state.setDuration(game.getGhostDyingTime());
			s_points = s_dying[game.ghostIndex];
			game.score += game.getGhostValue();
			game.ghostIndex += 1;
		};

		sm.changeOnTimeout(State.DYING, State.DEAD);

		// DEAD

		sm.state(State.DEAD).update = state -> move();

		sm.change(State.DEAD, State.SAFE, () -> getTile().equals(homeTile));

		return sm;
	}
}