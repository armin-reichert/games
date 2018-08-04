package de.amr.games.pacman.ui.actor;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.core.GameEvent;
import de.amr.games.pacman.controller.event.game.GhostKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManLosesPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Spritesheet;
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
		currentSprite = s_color[getDir()];
	}

	public GhostName getName() {
		return name;
	}

	// Sprites

	private Sprite currentSprite;
	private Sprite s_color[] = new Sprite[4];
	private Sprite s_eyes[] = new Sprite[4];
	private Sprite s_awed;
	private Sprite s_blinking;
	private Sprite s_numbers[] = new Sprite[4];

	private void createSprites(int color) {
		int size = 2 * TS;
		TOPOLOGY.dirs().forEach(dir -> {
			s_color[dir] = Spritesheet.ghostColored(color, dir).scale(size);
			s_eyes[dir] = Spritesheet.ghostEyes(dir).scale(size);
		});
		for (int i = 0; i < 4; ++i) {
			s_numbers[i] = Spritesheet.greenNumber(i).scale(size);
		}
		s_awed = Spritesheet.ghostAwed().scale(size);
		s_blinking = Spritesheet.ghostBlinking().scale(size);
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_color), Stream.of(s_numbers), Stream.of(s_eyes), Stream.of(s_awed, s_blinking))
				.flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return currentSprite;
	}

	// State machine

	public enum State {
		INITIAL, AGGRO, SCATTERING, AFRAID, DYING, DEAD, SAFE
	}

	@Override
	public StateMachine<State, GameEvent> getStateMachine() {
		return sm;
	}

	protected StateMachine<State, GameEvent> createStateMachine() {
		StateMachine<State, GameEvent> sm = new StateMachine<>(getName().toString(), State.class, State.INITIAL);

		// INITIAL

		sm.state(State.INITIAL).entry = state -> {
			setMazePosition(homeTile);
			getSprites().forEach(Sprite::resetAnimation);
			setSpeed(game::getGhostSpeed);
		};

		sm.state(State.INITIAL).change(State.SAFE);

		// SAFE

		sm.state(State.SAFE).entry = state -> {
			state.setDuration(game.sec(2));
		};

		sm.state(State.SAFE).update = state -> {
			move();
			currentSprite = s_color[getDir()];
		};

		sm.state(State.SAFE).changeOnTimeout(State.AGGRO, () -> pacMan.getState() != PacMan.State.EMPOWERED);

		sm.state(State.SAFE).changeOnTimeout(State.AFRAID, () -> pacMan.getState() == PacMan.State.EMPOWERED);

		// AGGRO

		sm.state(State.AGGRO).update = state -> {
			move();
			currentSprite = s_color[getDir()];
		};

		sm.state(State.AGGRO).changeOnInput(PacManGainsPowerEvent.class, State.AFRAID);

		// AFRAID

		sm.state(State.AFRAID).update = state -> {
			move();
			currentSprite = s_awed;
		};

		sm.state(State.AFRAID).changeOnInput(PacManLosesPowerEvent.class, State.AFRAID, t -> {
			currentSprite = s_blinking;
		});

		sm.state(State.AFRAID).changeOnInput(PacManLostPowerEvent.class, State.AGGRO);

		sm.state(State.AFRAID).changeOnInput(GhostKilledEvent.class, State.DYING);

		// DYING

		sm.state(State.DYING).entry = state -> {
			state.setDuration(game.getGhostDyingTime());
			currentSprite = s_numbers[game.ghostIndex];
			game.score += game.getGhostValue();
			game.ghostIndex += 1;
		};

		sm.state(State.DYING).changeOnTimeout(State.DEAD);

		// DEAD

		sm.state(State.DEAD).entry = state -> {
			currentSprite = s_eyes[getDir()];
		};

		sm.state(State.DEAD).update = state -> {
			move();
			currentSprite = s_eyes[getDir()];
		};

		sm.state(State.DEAD).change(State.SAFE, () -> getTile().equals(homeTile));

		return sm;
	}
}