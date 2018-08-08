package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.Ghost.State.*;
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

	private final StateMachine<State, GameEvent> sm;
	private final GhostName name;
	private final PacMan pacMan;

	public Ghost(GhostName name, PacMan pacMan, Game game, GameEventManager eventMgr, Tile home, int color) {
		super(game, eventMgr, home, new EnumMap<>(State.class));
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
		return StateMachine.define(State.class, GameEvent.class)
			 
			.description(String.format("[%s]", getName()))
			.initialState(State.INITIAL)
		
			.states()

				.state(State.INITIAL)
					.onEntry(() -> {
						setMazePosition(homeTile);
						getSprites().forEach(Sprite::resetAnimation);
						setSpeed(game::getGhostSpeed);
					})
				
				.state(State.AFRAID)
					.onTick(() -> {
						move();
						currentSprite = s_awed;
					})
				
				.state(State.AGGRO)
					.onTick(() -> {
						move();
						currentSprite = s_color[getDir()];
					})
				
				.state(State.DEAD)
					.onEntry(() -> {
						currentSprite = s_eyes[getDir()];
					})
					.onTick(() -> {
						move();
						currentSprite = s_eyes[getDir()];
					})
				
				.state(State.DYING)
					.timeout(game::getGhostDyingTime)
					.onEntry(() -> {
						currentSprite = s_numbers[game.ghostIndex];
						game.score += game.getGhostValue();
						game.ghostIndex += 1;
					})
				
				.state(State.SAFE)
					.timeout(() -> game.sec(2))
					.onEntry(() -> {
					})
					.onTick(() -> {
						move();
						currentSprite = s_color[getDir()];
					})
				
				.state(State.SCATTERING)
				
			.transitions()
				
				.when(INITIAL).become(SAFE)

				.when(SAFE).become(AGGRO)
					.onTimeout()
					.incase(() -> pacMan.getState() != PacMan.State.EMPOWERED)
				
				.when(SAFE).become(AFRAID)
					.onTimeout()
					.incase(() -> pacMan.getState() == PacMan.State.EMPOWERED)
				
				.when(State.SAFE)
					.on(PacManGainsPowerEvent.class)
					
				.when(State.SAFE)
					.on(PacManLosesPowerEvent.class)
				
				.when(State.SAFE)
					.on(PacManLostPowerEvent.class)
					
				.when(AGGRO).become(AFRAID)
					.on(PacManGainsPowerEvent.class)
					
				.when(AFRAID).become(AFRAID)
					.on(PacManLosesPowerEvent.class)
					.act(t -> {
						currentSprite = s_blinking;
					})

				.when(AFRAID)
					.on(PacManGainsPowerEvent.class)
					
				.when(AFRAID).become(AGGRO)
					.on(PacManLostPowerEvent.class)
					
				.when(AFRAID).become(DYING)
					.on(GhostKilledEvent.class)
					
				.when(DYING).become(DEAD)
					.onTimeout()
					
				.when(DEAD).become(SAFE)
					.incase(() -> getTile().equals(homeTile))
		
			.endStateMachine();
		/*@formatter:on*/
	}
}