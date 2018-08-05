package de.amr.games.pacman.actor;

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
import de.amr.statemachine.StateMachineBuilder;

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

		StateMachineBuilder<State, GameEvent> builder = new StateMachineBuilder<>(getName().toString(), State.class,
				State.INITIAL);
		
		/*@formatter:off*/
		StateMachine<State, GameEvent> sm = builder
		
			.states()

				.state(State.INITIAL)
					.onEntry(state -> {
						setMazePosition(homeTile);
						getSprites().forEach(Sprite::resetAnimation);
						setSpeed(game::getGhostSpeed);
					})
				.build()
				
				.state(State.AFRAID)
					.onTick(state -> {
						move();
						currentSprite = s_awed;
					})
				.build()
				
				.state(State.AGGRO)
					.onTick(state -> {
						move();
						currentSprite = s_color[getDir()];
					})
				.build()
				
				.state(State.DEAD)
					.onEntry(state -> {
						currentSprite = s_eyes[getDir()];
					})
					.onTick(state -> {
						move();
						currentSprite = s_eyes[getDir()];
					})
				.build()
				
				.state(State.DYING)
					.onEntry(state -> {
						state.setDuration(game.getGhostDyingTime());
						currentSprite = s_numbers[game.ghostIndex];
						game.score += game.getGhostValue();
						game.ghostIndex += 1;
					})
				.build()
				
				.state(State.SAFE)
					.onEntry(state -> {
						state.setDuration(game.sec(2));
					})
					.onTick(state -> {
						move();
						currentSprite = s_color[getDir()];
					})
				.build()
				
				.state(State.SCATTERING)
				.build()
				
			.transitions()
				.change(State.INITIAL, State.SAFE)
					.build()

				.onTimeout()
					.change(State.SAFE, State.AGGRO)
					.when(() -> pacMan.getState() != PacMan.State.EMPOWERED)
					.build()
				
				.onTimeout()
					.change(State.SAFE, State.AFRAID)
					.when(() -> pacMan.getState() == PacMan.State.EMPOWERED)
					.build()
				
				.on(PacManGainsPowerEvent.class)
					.change(State.AGGRO, State.AFRAID)
					.build()
					
				.on(PacManLosesPowerEvent.class)
					.change(State.AFRAID, State.AFRAID)
					.act(t -> {
						currentSprite = s_blinking;
					})
					.build()
					
				.on(PacManLostPowerEvent.class)
					.change(State.AFRAID, State.AGGRO)
					.build()
					
				.on(GhostKilledEvent.class)
					.change(State.AFRAID, State.DYING)
					.build()
					
				.onTimeout()
					.change(State.DYING, State.DEAD)
					.build()
					
				.when(() -> getTile().equals(homeTile))
					.change(State.DEAD, State.SAFE)
					.build()
		
			.buildStateMachine();
		/*@formatter:on*/
		
		return sm;
	}
}