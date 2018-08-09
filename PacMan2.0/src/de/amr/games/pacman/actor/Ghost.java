package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.Ghost.State.AFRAID;
import static de.amr.games.pacman.actor.Ghost.State.AGGRO;
import static de.amr.games.pacman.actor.Ghost.State.DEAD;
import static de.amr.games.pacman.actor.Ghost.State.DYING;
import static de.amr.games.pacman.actor.Ghost.State.HOME;
import static de.amr.games.pacman.actor.Ghost.State.SAFE;
import static de.amr.games.pacman.actor.Ghost.State.SCATTERING;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.Spritesheet.TS;

import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pacman.controller.event.game.GameEvent;
import de.amr.games.pacman.controller.event.game.GhostKilledEvent;
import de.amr.games.pacman.controller.event.game.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.game.PacManGettingWeakerEvent;
import de.amr.games.pacman.controller.event.game.PacManLostPowerEvent;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.GameUI;
import de.amr.statemachine.StateMachine;

public class Ghost extends MazeMover<Ghost.State> {

	private final StateMachine<State, GameEvent> sm;
	private final GhostName name;
	private final PacMan pacMan;

	public Ghost(GhostName name, PacMan pacMan, Game game, Tile home, int color) {
		super(game, home, new EnumMap<>(State.class));
		this.pacMan = pacMan;
		this.name = name;
		sm = buildStateMachine();
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
			s_color[dir] = GameUI.SPRITES.ghostColored(color, dir).scale(size);
			s_eyes[dir] = GameUI.SPRITES.ghostEyes(dir).scale(size);
		});
		for (int i = 0; i < 4; ++i) {
			s_numbers[i] = GameUI.SPRITES.greenNumber(i).scale(size);
		}
		s_awed = GameUI.SPRITES.ghostAwed().scale(size);
		s_blinking = GameUI.SPRITES.ghostBlinking().scale(size);
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
		HOME, AGGRO, SCATTERING, AFRAID, DYING, DEAD, SAFE
	}

	@Override
	public StateMachine<State, GameEvent> getStateMachine() {
		return sm;
	}

	private StateMachine<State, GameEvent> buildStateMachine() {
		/*@formatter:off*/
		return StateMachine.define(State.class, GameEvent.class)
			 
			.description(String.format("[%s]", getName()))
			.initialState(HOME)
		
			.states()

				.state(HOME)
					.onEntry(() -> {
						setMazePosition(homeTile);
						getSprites().forEach(Sprite::resetAnimation);
						setSpeed(game::getGhostSpeed);
					})
				
				.state(AFRAID)
					.onEntry(() -> currentSprite = s_awed)
					.onTick(() -> move())
				
				.state(AGGRO)
					.onTick(() -> {
						move();
						currentSprite = s_color[getDir()];
					})
				
				.state(DEAD)
					.onEntry(() -> currentSprite = s_eyes[getDir()])
					.onTick(() -> {
						move();
						currentSprite = s_eyes[getDir()];
					})
				
				.state(DYING)
					.onEntry(() -> {
						currentSprite = s_numbers[game.ghostIndex];
						game.score += game.getGhostValue();
						game.ghostIndex += 1;
					})
					.timeout(game::getGhostDyingTime)
				
				.state(SAFE)
					.timeout(() -> game.sec(2))
					.onTick(() -> {
						move();
						currentSprite = s_color[getDir()];
					})
				
				.state(SCATTERING) //TODO
				
			.transitions()
				
				.when(HOME).become(SAFE)

				.when(SAFE).become(AGGRO)
					.onTimeout()
					.inCase(() -> pacMan.getState() != PacMan.State.STEROIDS)
				
				.when(SAFE).become(AFRAID)
					.onTimeout()
					.inCase(() -> pacMan.getState() == PacMan.State.STEROIDS)
				
				.when(SAFE)
					.on(PacManGainsPowerEvent.class)
					
				.when(SAFE)
					.on(PacManGettingWeakerEvent.class)
				
				.when(SAFE)
					.on(PacManLostPowerEvent.class)
					
				.when(AGGRO).become(AFRAID)
					.on(PacManGainsPowerEvent.class)
					
				.when(AFRAID)
					.on(PacManGettingWeakerEvent.class)
					.act(e -> currentSprite = s_blinking)

				.when(AFRAID)
					.on(PacManGainsPowerEvent.class)
					
				.when(AFRAID).become(AGGRO)
					.on(PacManLostPowerEvent.class)
					
				.when(AFRAID).become(DYING)
					.on(GhostKilledEvent.class)
					
				.when(DYING).become(DEAD)
					.onTimeout()
					
				.when(DEAD)
					.on(PacManGettingWeakerEvent.class)
		
				.when(DEAD).become(SAFE)
					.inCase(() -> getTile().equals(homeTile))
		
			.endStateMachine();
		/*@formatter:on*/
	}
}