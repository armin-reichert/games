package de.amr.games.pacman.play;

import static de.amr.games.pacman.play.GhostAttackState.Chasing;
import static de.amr.games.pacman.play.GhostAttackState.Initialized;
import static de.amr.games.pacman.play.GhostAttackState.Scattering;

import java.util.EnumMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import de.amr.easy.game.Application;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.statemachine.StateMachine;

/**
 * State machine which controls the timing of the ghost attacks.
 */
public class GhostAttackTimer {

	private static final int[][] SCATTERING_TIMES = {
		/*@formatter:off*/
		{ 7, 7, 5, 5, 0 }, 	// level 1
		{ 7, 7, 5, 0, 0 }, 	// level 2-4
		{ 5, 5, 5, 0, 0 }   // level 5...
		/*@formatter:on*/
	};

	private static final int[][] CHASING_TIMES = {
		/*@formatter:off*/
		{ 20, 20, 20, 	Integer.MAX_VALUE },  // level 1 
		{ 20, 20, 1033, Integer.MAX_VALUE },	// level 2-4
		{ 20, 20, 1037, Integer.MAX_VALUE } 	// level 5...
		/*@formatter:on*/
	};

	private final StateMachine<GhostAttackState> fsm;
	private final Application app;
	private int level;
	private int wave;

	private int getPhaseDuration(int[][] times) {
		int row = (level == 1) ? 0 : (level <= 4) ? 1 : 2;
		int n = times[0].length, col = wave <= n ? wave - 1 : n - 1;
		return app.motor.toFrames(times[row][col]);
	}

	public void setLogger(Logger logger) {
		fsm.setLogger(logger, app.motor.getFrequency());
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	public void init() {
		fsm.changeTo(Initialized);
	}

	public void start() {
		if (!fsm.inState(Initialized)) {
			init();
		}
		fsm.changeTo(Scattering);
	}

	public void update() {
		fsm.update();
	}

	public GhostAttackState state() {
		return fsm.stateID();
	}

	public GhostAttackTimer(Application app, Ghost... ghosts) {

		this.app = app;

		fsm = new StateMachine<>("GhostAttackTimer", new EnumMap<>(GhostAttackState.class));

		fsm.state(Initialized).entry = state -> {
			wave = 1;
		};

		fsm.state(Scattering).entry = state -> {
			state.setDuration(getPhaseDuration(SCATTERING_TIMES));
			Stream.of(ghosts).forEach(Ghost::beginScattering);
			app.assets.sound("sfx/siren.mp3").loop();
		};

		fsm.state(Scattering).update = state -> {
			if (state.isTerminated()) {
				fsm.changeTo(Chasing);
			}
		};

		fsm.state(Scattering).exit = state -> {
			app.assets.sound("sfx/siren.mp3").stop();
		};

		fsm.state(Chasing).entry = state -> {
			state.setDuration(getPhaseDuration(CHASING_TIMES));
			Stream.of(ghosts).forEach(Ghost::beginChasing);
			app.assets.sound("sfx/siren.mp3").loop();
		};

		fsm.state(Chasing).update = state -> {
			if (state.isTerminated()) {
				fsm.changeTo(Scattering);
			}
		};

		fsm.state(Chasing).exit = state -> {
			app.assets.sound("sfx/siren.mp3").stop();
			++wave;
		};
	}
}