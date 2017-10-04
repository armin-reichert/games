package de.amr.games.pacman2.play.controller;

import static de.amr.games.pacman2.play.controller.PacmanGameEvent.COIN_INSERTED;
import static de.amr.games.pacman2.play.controller.PacmanGameEvent.LEVEL_START_COMPLETED;
import static de.amr.games.pacman2.play.controller.PacmanGameEvent.PACMAN_DIES;
import static de.amr.games.pacman2.play.controller.PacmanGameState.GAME_OVER;
import static de.amr.games.pacman2.play.controller.PacmanGameState.PACMAN_DYING;
import static de.amr.games.pacman2.play.controller.PacmanGameState.READY;
import static de.amr.games.pacman2.play.controller.PacmanGameState.RUNNING;
import static de.amr.games.pacman2.play.controller.PacmanGameState.STARTING_LEVEL;
import static de.amr.games.pacman2.play.controller.PacmanGameState.WAIT_FOR_COIN;

import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;

public abstract class PacmanGameStateMachine extends StateMachine<PacmanGameState, PacmanGameEvent> {

	public PacmanGameStateMachine() {
		super("PacManGameStateMachine", PacmanGameState.class, WAIT_FOR_COIN);

		state(WAIT_FOR_COIN).entry = this::resetGame;

		changeOnInput(COIN_INSERTED, WAIT_FOR_COIN, READY);

		change(READY, STARTING_LEVEL, this::isReadyForPlaying);

		changeOnInput(LEVEL_START_COMPLETED, STARTING_LEVEL, RUNNING);

		changeOnInput(PACMAN_DIES, RUNNING, GAME_OVER, this::canPacmanDie);

		changeOnInput(PACMAN_DIES, RUNNING, PACMAN_DYING);

		changeOnTimeout(PACMAN_DYING, RUNNING);

	}

	protected abstract void resetGame(State s);

	protected abstract boolean canPacmanDie();

	protected abstract boolean isReadyForPlaying();
}
