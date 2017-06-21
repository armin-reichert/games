package de.amr.games.pacman.core.entities.ghost.behaviors;

import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Chasing;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Frightened;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Initialized;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Scattering;
import static de.amr.games.pacman.core.entities.ghost.behaviors.GhostState.Waiting;

import java.util.EnumMap;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import de.amr.games.pacman.core.statemachine.StateMachine;

public class GhostControl extends StateMachine<GhostState, String> {

	private Logger log = Logger.getGlobal();

	public GhostControl() {
		super("GhostControl", new EnumMap<>(GhostState.class), Initialized);
		setLogger(log, 60);
		changeOnInput("GameReady", Initialized, Waiting);
		changeOnInput("ScatteringPhaseStarted", Waiting, Scattering);
		changeOnInput("PacManAttackStarts", Waiting, Frightened);
		changeOnInput("ChasingPhaseStarted", Scattering, Chasing);
		changeOnInput("PacManAttackStarts", Scattering, Frightened);
		changeOnInput("ScatteringPhaseStarted", Chasing, Scattering);
		changeOnInput("PacManAttackStarts", Chasing, Frightened);
		changeOnInput("PacManAttackEnds", Frightened, getStateAfterFrightened());
	}

	private GhostState getStateAfterFrightened() {
		return Waiting;
	}

	public static void main(String[] args) {
		StateMachine<GhostState, String> control = new GhostControl();
		control.init();
		control.addInput("GameReady");
		control.addInput("ScatteringPhaseStarted");
		control.addInput("PacManAttackStarts");
		control.addInput("ChasingPhaseStarted");
		control.addInput("PacManAttackEnds");
		IntStream.rangeClosed(1, 20).forEach(i -> control.update());
	}
}
