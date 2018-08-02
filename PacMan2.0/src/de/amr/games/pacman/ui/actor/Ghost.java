package de.amr.games.pacman.ui.actor;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.TS;
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
import de.amr.games.pacman.controller.event.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.statemachine.StateMachine;

public class Ghost extends MazeMover<Ghost.State> {

	private final StateMachine<State, GameEvent> sm;
	private final GhostName name;
	private final PacMan pacMan;

	public Ghost(GhostName name, PacMan pacMan, Game game, Maze maze, Tile home, int color) {
		super(game, maze, home, new EnumMap<>(State.class));
		this.pacMan = pacMan;
		this.name = name;
		sm = createStateMachine();
		createSprites(color);
	}

	public GhostName getName() {
		return name;
	}

	// Sprites

	private Sprite currentSprite;
	private Sprite s_color[] = new Sprite[4];
	private Sprite s_number[] = new Sprite[4];
	private Sprite s_eyes[] = new Sprite[4];
	private Sprite s_blue;
	private Sprite s_blinking;

	private void createSprites(int color) {
		int size = 2 * TS;
		TOPOLOGY.dirs().forEach(dir -> {
			s_color[dir] = new Sprite(getGhostNormal(color, dir)).scale(size)
					.animation(AnimationMode.BACK_AND_FORTH, 300);
			s_eyes[dir] = new Sprite(getGhostEyes(dir)).scale(size);
		});
		for (int i = 0; i < 4; ++i) {
			s_number[i] = new Sprite(getGreenNumber(i)).scale(size);
		}
		s_blue = new Sprite(getGhostBlue()).scale(size).animation(AnimationMode.CYCLIC, 200);
		s_blinking = new Sprite(getGhostBlueWhite()).scale(size).animation(AnimationMode.CYCLIC, 100);
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream
				.of(Stream.of(s_color), Stream.of(s_number), Stream.of(s_eyes), Stream.of(s_blue, s_blinking))
				.flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return currentSprite;
	}

	// State machine

	public enum State {
		AGGRO, SCATTERING, AFRAID, DYING, DEAD, SAFE
	}

	@Override
	public StateMachine<State, GameEvent> getStateMachine() {
		return sm;
	}

	protected StateMachine<State, GameEvent> createStateMachine() {
		StateMachine<State, GameEvent> sm = new StateMachine<>(getName().toString(), State.class, State.SAFE);

		// SAFE

		sm.state(State.SAFE).entry = state -> {
			state.setDuration(game.sec(2));
			currentSprite = s_color[getDir()];
		};

		sm.state(State.SAFE).update = state -> move();

		sm.changeOnTimeout(State.SAFE, State.AGGRO, () -> pacMan.getState() != PacMan.State.EMPOWERED);

		sm.changeOnTimeout(State.SAFE, State.AFRAID, () -> pacMan.getState() == PacMan.State.EMPOWERED);

		sm.changeOnInput(PacManLosesPowerEvent.class, State.SAFE, State.AGGRO);

		// AGGRO

		sm.state(State.AGGRO).entry = state -> {
			currentSprite = s_color[getDir()];
		};

		sm.state(State.AGGRO).update = state -> move();

		sm.changeOnInput(PacManGainsPowerEvent.class, State.AGGRO, State.AFRAID);

		// AFRAID

		sm.state(State.AFRAID).entry = state -> {
			currentSprite = s_blue;
		};

		sm.state(State.AFRAID).update = state -> move();

		sm.changeOnInput(PacManLosesPowerEvent.class, State.AFRAID, State.AFRAID, t -> {
			currentSprite = s_blinking;
		});

		sm.changeOnInput(PacManLostPowerEvent.class, State.AFRAID, State.AGGRO);

		sm.changeOnInput(GhostKilledEvent.class, State.AFRAID, State.DYING);

		// DYING

		sm.state(State.DYING).entry = state -> {
			state.setDuration(game.getGhostDyingTime());
			currentSprite = s_number[game.ghostIndex];
			game.score += game.getGhostValue();
			game.ghostIndex += 1;
		};

		sm.changeOnTimeout(State.DYING, State.DEAD);

		// DEAD

		sm.state(State.DEAD).entry = state -> {
			currentSprite = s_eyes[getDir()];
		};

		sm.state(State.DEAD).update = state -> move();

		sm.change(State.DEAD, State.SAFE, () -> getTile().equals(homeTile));

		return sm;
	}
}