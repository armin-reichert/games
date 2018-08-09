package de.amr.games.pacman.actor;

import static de.amr.games.pacman.actor.Ghost.State.AFRAID;
import static de.amr.games.pacman.actor.Ghost.State.AGGRO;
import static de.amr.games.pacman.actor.Ghost.State.DEAD;
import static de.amr.games.pacman.actor.Ghost.State.DYING;
import static de.amr.games.pacman.actor.Ghost.State.HOME;
import static de.amr.games.pacman.actor.Ghost.State.SAFE;
import static de.amr.games.pacman.actor.Ghost.State.SCATTERING;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.ui.GameUI.SPRITES;
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
import de.amr.statemachine.StateMachine;

public class Ghost extends MazeMover<Ghost.State> {

	private final StateMachine<State, GameEvent> sm;
	private final GameActors.Name name;
	private final PacMan pacMan;
	private final int initialDir;

	public Ghost(GameActors.Name name, PacMan pacMan, Game game, Tile home, int initialDir, int color) {
		super(game, home, new EnumMap<>(State.class));
		this.pacMan = pacMan;
		this.name = name;
		this.initialDir = initialDir;
		sm = buildStateMachine();
		createSprites(color);
		s_current = s_color[getDir()];
	}

	public GameActors.Name getName() {
		return name;
	}

	private void initGhost() {
		placeAt(homeTile);
		setDir(initialDir);
		setNextDir(initialDir);
		getSprites().forEach(Sprite::resetAnimation);
		setSpeed(game::getGhostSpeed);
	}

	// Sprites

	private Sprite s_current;
	private Sprite s_color[] = new Sprite[4];
	private Sprite s_eyes[] = new Sprite[4];
	private Sprite s_awed;
	private Sprite s_blinking;
	private Sprite s_numbers[] = new Sprite[4];

	private void createSprites(int color) {
		int size = 2 * TS;
		TOPOLOGY.dirs().forEach(dir -> {
			s_color[dir] = SPRITES.ghostColored(color, dir).scale(size);
			s_eyes[dir] = SPRITES.ghostEyes(dir).scale(size);
		});
		for (int i = 0; i < 4; ++i) {
			s_numbers[i] = SPRITES.greenNumber(i).scale(size);
		}
		s_awed = SPRITES.ghostAwed().scale(size);
		s_blinking = SPRITES.ghostBlinking().scale(size);
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(Stream.of(s_color), Stream.of(s_numbers), Stream.of(s_eyes), Stream.of(s_awed, s_blinking))
				.flatMap(s -> s);
	}

	@Override
	public Sprite currentSprite() {
		return s_current;
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
		return
		/*@formatter:off*/
		StateMachine.define(State.class, GameEvent.class)
			 
			.description(String.format("[Ghost %s]", getName()))
			.initialState(HOME)
		
			.states()

					.state(HOME)
						.onEntry(this::initGhost)
					
					.state(AFRAID)
						.onTick(() -> { move(); s_current = s_awed; })
					
					.state(AGGRO)
						.onTick(() -> {	move();	s_current = s_color[getDir()]; })
					
					.state(DEAD)
						.onTick(() -> {	move();	s_current = s_eyes[getDir()]; })
					
					.state(DYING)
						.onEntry(() -> s_current = s_numbers[game.ghostsKilledInSeries] )
						.timeoutAfter(game::getGhostDyingTime)
					
					.state(SAFE)
						.onTick(() -> {	move();	s_current = s_color[getDir()]; })
						.timeoutAfter(() -> game.sec(2))
					
					.state(SCATTERING) //TODO
				
			.transitions()

					.when(HOME).then(SAFE)

					.when(SAFE)
						.onTimeout().condition(() -> pacMan.getState() != PacMan.State.STEROIDS)
						.then(AGGRO)
						
					.when(SAFE)
						.onTimeout().condition(() -> pacMan.getState() == PacMan.State.STEROIDS)
						.then(AFRAID)
						
					.stay(SAFE).on(PacManGainsPowerEvent.class)
					.stay(SAFE).on(PacManGettingWeakerEvent.class)
					.stay(SAFE).on(PacManLostPowerEvent.class)
						
					.when(AGGRO).on(PacManGainsPowerEvent.class).then(AFRAID)
						
					.stay(AFRAID).on(PacManGettingWeakerEvent.class).act(e -> s_current = s_blinking)
					.stay(AFRAID).on(PacManGainsPowerEvent.class)
					.when(AFRAID).on(PacManLostPowerEvent.class).then(AGGRO)
					.when(AFRAID).on(GhostKilledEvent.class).then(DYING)
						
					.when(DYING).then(DEAD).onTimeout()
						
					.stay(DEAD).on(PacManGettingWeakerEvent.class)
					.stay(DEAD).on(PacManLostPowerEvent.class)
					.when(DEAD).condition(() -> getTile().equals(homeTile)).then(SAFE)

		.endStateMachine();
		/*@formatter:on*/
	}
}