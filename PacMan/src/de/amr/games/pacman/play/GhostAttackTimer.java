package de.amr.games.pacman.play;

import static de.amr.games.pacman.play.GhostAttackState.Chasing;
import static de.amr.games.pacman.play.GhostAttackState.Initialized;
import static de.amr.games.pacman.play.GhostAttackState.Scattering;

import java.util.EnumMap;
import java.util.function.Consumer;

import de.amr.easy.game.Application;
import de.amr.games.pacman.core.statemachine.StateMachine;

/**
 * State machine which controls the timing of the ghost attacks.
 */
public class GhostAttackTimer {

	private static final int[][] SCATTERING_TIME = {
		/*@formatter:off*/
		{ 7, 7, 5, 5, 0 }, 	// level 1
		{ 7, 7, 5, 0, 0 }, 	// level 2-4
		{ 5, 5, 5, 0, 0 }   // level 5...
		/*@formatter:on*/
	};

	private static final int[][] CHASING_TIME = {
		/*@formatter:off*/
		{ 20, 20, 20, 	Integer.MAX_VALUE },  // level 1 
		{ 20, 20, 1033, Integer.MAX_VALUE },	// level 2-4
		{ 20, 20, 1037, Integer.MAX_VALUE } 	// level 5...
		/*@formatter:on*/
	};

	public Consumer<GhostAttackState> onPhaseStart;
	public Consumer<GhostAttackState> onPhaseEnd;

	public boolean trace;

	private final StateMachine<GhostAttackState> fsm;
	private final Application app;
	private int level;
	private int wave;

	private int getPhaseDuration(int[][] timeTable) {
		int row = (level == 1) ? 0 : (level <= 4) ? 1 : 2;
		int n = timeTable[0].length, col = wave <= n ? wave - 1 : n - 1;
		return app.motor.toFrames(timeTable[row][col]);
	}

	public void init(int level) {
		if (!fsm.inState(Initialized)) {
			this.level = level;
			fsm.changeTo(Initialized);
		}
	}

	public void start() {
		if (!fsm.inState(Initialized)) {
			throw new IllegalStateException("Attack control FSM not initialized");
		}
		fsm.changeTo(Scattering);
	}

	public void update() {
		fsm.update();
	}

	public GhostAttackState state() {
		return fsm.stateID();
	}

	public GhostAttackTimer(Application app) {

		this.app = app;

		onPhaseStart = state -> {
		};

		onPhaseEnd = state -> {
		};

		fsm = new StateMachine<>("GhostAttackTimer", new EnumMap<>(GhostAttackState.class));

		fsm.state(Initialized).entry = state -> {
			wave = 1;
			onPhaseStart.accept(fsm.stateID());
			traceEntry();
		};

		fsm.state(Initialized).exit = state -> {
			onPhaseEnd.accept(fsm.stateID());
			traceExit();
		};

		fsm.state(Scattering).entry = state -> {
			state.setDuration(getPhaseDuration(SCATTERING_TIME));
			onPhaseStart.accept(fsm.stateID());
			traceEntry();
		};

		fsm.state(Scattering).update = state -> {
			if (state.isTerminated()) {
				fsm.changeTo(Chasing);
			}
		};

		fsm.state(Scattering).exit = state -> {
			onPhaseEnd.accept(fsm.stateID());
			traceExit();
		};

		fsm.state(Chasing).entry = state -> {
			state.setDuration(getPhaseDuration(CHASING_TIME));
			onPhaseStart.accept(fsm.stateID());
			traceEntry();
		};

		fsm.state(Chasing).update = state -> {
			if (state.isTerminated()) {
				fsm.changeTo(Scattering);
			}
		};

		fsm.state(Chasing).exit = state -> {
			onPhaseEnd.accept(fsm.stateID());
			traceExit();
			++wave;
		};
	}

	private void traceEntry() {
		if (trace) {
			Application.Log.info(String.format("Start of phase '%s' (%.2f seconds)", fsm.stateID(),
					app.motor.toSeconds(fsm.state().getDuration())));
		}
	}

	private void traceExit() {
		if (trace) {
			Application.Log.info(String.format("End of phase '%s'", fsm.stateID()));
		}
	}
}