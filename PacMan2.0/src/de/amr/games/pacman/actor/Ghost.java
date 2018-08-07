package de.amr.games.pacman.actor;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.core.GameEvent;
import de.amr.games.pacman.controller.event.core.GameEventManager;
import de.amr.games.pacman.controller.event.game.GhostKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManLosesPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Spritesheet;
import de.amr.statemachine.StateMachine;

public class Ghost extends MazeMover<Ghost.State> {

	public final GameEventManager eventMgr;
	private final StateMachine<State, GameEvent> sm;
	private final GhostName name;
	private final PacMan pacMan;

	public Ghost(GhostName name, PacMan pacMan, Game game, Tile home, int color) {
		super(game, home, new EnumMap<>(State.class));
		this.pacMan = pacMan;
		this.name = name;
		sm = buildStateMachine();
		eventMgr = new GameEventManager(name.toString());
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

	private StateMachine<State, GameEvent> buildStateMachine() {
		/*@formatter:off*/
		return StateMachine.builder(State.class, GameEvent.class)
			 
			.description(String.format("[%s]", getName()))
			.initialState(State.INITIAL)
		
			.states()

				.state(State.INITIAL)
					.onEntry(() -> {
						setMazePosition(homeTile);
						getSprites().forEach(Sprite::resetAnimation);
						setSpeed(game::getGhostSpeed);
					})
				.build()
				
				.state(State.AFRAID)
					.onTick(() -> {
						move();
						currentSprite = s_awed;
					})
				.build()
				
				.state(State.AGGRO)
					.onTick(() -> {
						move();
						currentSprite = s_color[getDir()];
					})
				.build()
				
				.state(State.DEAD)
					.onEntry(() -> {
						currentSprite = s_eyes[getDir()];
					})
					.onTick(() -> {
						move();
						currentSprite = s_eyes[getDir()];
					})
				.build()
				
				.state(State.DYING)
					.duration(game::getGhostDyingTime)
					.onEntry(() -> {
						currentSprite = s_numbers[game.ghostIndex];
						game.score += game.getGhostValue();
						game.ghostIndex += 1;
					})
				.build()
				
				.state(State.SAFE)
					.duration(() -> game.sec(2))
					.onEntry(() -> {
					})
					.onTick(() -> {
						move();
						currentSprite = s_color[getDir()];
					})
				.build()
				
				.state(State.SCATTERING)
				.build()
				
			.transitions()
				
				.change(State.INITIAL, State.SAFE)
					.build()

				.change(State.SAFE, State.AGGRO)
					.onTimeout()
					.when(() -> pacMan.getState() != PacMan.State.EMPOWERED)
					.build()
				
				.change(State.SAFE, State.AFRAID)
					.onTimeout()
					.when(() -> pacMan.getState() == PacMan.State.EMPOWERED)
					.build()

				
				.keep(State.SAFE)
					.on(PacManGainsPowerEvent.class)
					.build()
					
				.keep(State.SAFE)
					.on(PacManLosesPowerEvent.class)
					.build()
				
				.keep(State.SAFE)
					.on(PacManLostPowerEvent.class)
					.build()
					
				.change(State.AGGRO, State.AFRAID)
					.on(PacManGainsPowerEvent.class)
					.build()
					
				.change(State.AFRAID, State.AFRAID)
					.on(PacManLosesPowerEvent.class)
					.act(t -> {
						currentSprite = s_blinking;
					})
					.build()

				.keep(State.AFRAID)
					.on(PacManGainsPowerEvent.class)
					.build()
					
				.change(State.AFRAID, State.AGGRO)
					.on(PacManLostPowerEvent.class)
					.build()
					
				.change(State.AFRAID, State.DYING)
					.on(GhostKilledEvent.class)
					.build()
					
				.change(State.DYING, State.DEAD)
					.onTimeout()
					.build()
					
				.change(State.DEAD, State.SAFE)
					.when(() -> getTile().equals(homeTile))
					.build()
		
			.buildStateMachine();
		/*@formatter:on*/
	}
}