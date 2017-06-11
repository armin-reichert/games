package de.amr.games.pacman.play;

import static de.amr.games.pacman.play.GhostAttackState.Chasing;
import static de.amr.games.pacman.play.GhostAttackState.Initialized;
import static de.amr.games.pacman.play.GhostAttackState.Scattering;

import java.util.EnumMap;
import java.util.Set;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.statemachine.StateMachine;

/**
 * State machine which controls the timing of the ghost attacks.
 */
public class GhostAttackTimer {

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

	public GhostAttackTimer(Application app, Set<Ghost> ghosts, int[][] scatteringTimes, int[][] chasingTimes) {
		this.app = app;
		fsm = new StateMachine<>("GhostAttackTimer", new EnumMap<>(GhostAttackState.class));

		fsm.state(Initialized).entry = state -> {
			wave = 1;
		};

		fsm.state(Scattering).entry = state -> {
			state.setDuration(getPhaseDuration(scatteringTimes));
			ghosts.forEach(Ghost::beginScattering);
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
			state.setDuration(getPhaseDuration(chasingTimes));
			ghosts.forEach(Ghost::beginChasing);
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