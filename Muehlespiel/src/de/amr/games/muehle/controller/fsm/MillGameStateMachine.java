package de.amr.games.muehle.controller.fsm;

import static de.amr.games.muehle.controller.fsm.MillGameEvent.STONE_PLACED;
import static de.amr.games.muehle.controller.fsm.MillGameEvent.STONE_PLACED_IN_MILL;
import static de.amr.games.muehle.controller.fsm.MillGameEvent.STONE_REMOVED;
import static de.amr.games.muehle.controller.fsm.MillGamePhase.GAME_OVER;
import static de.amr.games.muehle.controller.fsm.MillGamePhase.MOVING;
import static de.amr.games.muehle.controller.fsm.MillGamePhase.MOVING_REMOVING;
import static de.amr.games.muehle.controller.fsm.MillGamePhase.PLACING;
import static de.amr.games.muehle.controller.fsm.MillGamePhase.PLACING_REMOVING;
import static de.amr.games.muehle.controller.fsm.MillGamePhase.STARTING;

import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.easy.statemachine.Transition;

/**
 * Abstract class defining the control structure of the game.
 * 
 * @author Armin Reichert
 *
 */
public abstract class MillGameStateMachine extends StateMachine<MillGamePhase, MillGameEvent> {

	protected abstract void resetGame(State state);

	protected abstract void tryToPlaceStone(State state);

	protected abstract void onMillClosedByPlacing(Transition<MillGamePhase, MillGameEvent> change);

	protected abstract boolean areAllStonesPlaced();

	protected abstract void switchMoving(Transition<MillGamePhase, MillGameEvent> change);

	protected abstract void switchPlacing(Transition<MillGamePhase, MillGameEvent> change);

	protected abstract void startRemoving(State state);

	protected abstract void tryToRemoveStone(State state);

	protected abstract void updateMove(State state);

	protected abstract boolean isMoveComplete();

	protected abstract boolean isMillClosedByMove();

	protected abstract boolean isGameOver();

	protected abstract void onGameOver(State state);

	protected abstract boolean shallStartNewGame();

	public MillGameStateMachine() {
		super("MillGameControl", MillGamePhase.class, STARTING);

		// STARTING

		state(STARTING).entry = this::resetGame;

		change(STARTING, PLACING);

		// PLACING

		state(PLACING).update = this::tryToPlaceStone;

		changeOnInput(STONE_PLACED_IN_MILL, PLACING, PLACING_REMOVING, this::onMillClosedByPlacing);

		changeOnInput(STONE_PLACED, PLACING, MOVING, this::areAllStonesPlaced, this::switchMoving);

		changeOnInput(STONE_PLACED, PLACING, PLACING, this::switchPlacing);

		// PLACING_REMOVING

		state(PLACING_REMOVING).entry = this::startRemoving;

		state(PLACING_REMOVING).update = this::tryToRemoveStone;

		changeOnInput(STONE_REMOVED, PLACING_REMOVING, MOVING, this::areAllStonesPlaced, this::switchMoving);

		changeOnInput(STONE_REMOVED, PLACING_REMOVING, PLACING, this::switchPlacing);

		// MOVING

		state(MOVING).update = this::updateMove;

		change(MOVING, GAME_OVER, this::isGameOver);

		change(MOVING, MOVING_REMOVING, this::isMillClosedByMove);

		change(MOVING, MOVING, this::isMoveComplete, this::switchMoving);

		// MOVING_REMOVING

		state(MOVING_REMOVING).entry = this::startRemoving;

		state(MOVING_REMOVING).update = this::tryToRemoveStone;

		changeOnInput(STONE_REMOVED, MOVING_REMOVING, MOVING, this::switchMoving);

		// GAME_OVER

		state(GAME_OVER).entry = this::onGameOver;

		change(GAME_OVER, STARTING, this::shallStartNewGame);
	}

}