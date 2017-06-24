package de.amr.games.pacman.play;

import static de.amr.games.pacman.play.GhostAttackState.Chasing;
import static de.amr.games.pacman.play.GhostAttackState.Initialized;
import static de.amr.games.pacman.play.GhostAttackState.Scattering;

import java.util.EnumMap;
import java.util.Set;
import java.util.logging.Logger;

import de.amr.easy.game.Application;
import de.amr.easy.game.timing.Motor;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostEvent;

/**
 * State machine which controls the timing of the ghost attacks.
 * 
 * @author Armin Reichert
 */
public class GhostAttackTimer {

	private final StateMachine<GhostAttackState,String> fsm;
	private final Motor motor;
	private int level;
	private int wave;

	private int computeFrames(int[][] times) {
		int row = (level == 1) ? 0 : (level <= 4) ? 1 : 2;
		int n = times[0].length, col = wave <= n ? wave - 1 : n - 1;
		return motor.secToTicks(times[row][col]);
	}

	public GhostAttackTimer(Application app, Set<Ghost> ghosts, int[][] scatteringSeconds, int[][] chasingSeconds) {
		this.motor = app.motor;

		fsm = new StateMachine<>("GhostAttackTimer", new EnumMap<>(GhostAttackState.class), Initialized);
		
		fsm.change(Initialized, Scattering, () -> true);

		fsm.state(Scattering).entry = state -> {
			++wave;
			state.setDuration(computeFrames(scatteringSeconds));
			ghosts.forEach(ghost -> ghost.receiveEvent(GhostEvent.ScatteringStarts));
			app.assets.sound("sfx/siren.mp3").stop();
			app.assets.sound("sfx/siren.mp3").loop();
		};

		fsm.changeOnTimeout(Scattering, Chasing);

		fsm.state(Chasing).entry = state -> {
			state.setDuration(computeFrames(chasingSeconds));
			ghosts.forEach(ghost -> ghost.receiveEvent(GhostEvent.ChasingStarts));
		};

		fsm.changeOnTimeout(Chasing, Scattering);
	}

	public void init() {
		wave = 0;
		fsm.init();
	}

	public void update() {
		fsm.update();
	}

	public GhostAttackState state() {
		return fsm.stateID();
	}

	public void setLogger(Logger logger) {
		fsm.setLogger(logger);
		fsm.setFrequency(motor.getFrequency());
	}

	public void setLevel(int level) {
		this.level = level;
	}
}