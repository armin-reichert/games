package de.amr.games.pacman.fsm;

import java.util.Map;
import java.util.function.Consumer;

import de.amr.easy.game.Application;

/**
 * A finite state machine.
 *
 * @param <StateID>
 *          type for identifying states, for example an enumeration type
 */
public class StateMachine<StateID> {

	private final String description;
	private final Map<StateID, State> statesByID;
	private StateID currentStateID;

	public StateMachine(Map<StateID, State> stateMap) {
		this("", stateMap);
	}

	public StateMachine(String description, Map<StateID, State> stateMap) {
		this.description = description;
		this.statesByID = stateMap;
	}

	@SuppressWarnings("unchecked")
	public boolean inState(StateID... stateIDs) {
		for (StateID stateID : stateIDs) {
			if (currentStateID == stateID) {
				return true;
			};
		}
		return false;
	}

	public StateID stateID() {
		return currentStateID;
	}

	public State state() {
		return state(currentStateID);
	}

	public State state(StateID stateID) {
		if (!statesByID.containsKey(stateID)) {
			statesByID.put(stateID, new State());
		}
		return statesByID.get(stateID);
	}

	public State state(StateID stateID, State state) {
		if (statesByID.containsKey(stateID)) {
			throw new IllegalStateException("State with ID '" + stateID + "' already exists");
		}
		statesByID.put(stateID, state);
		return state;
	}

	public void update() {
		state().doUpdate();
	}

	public void changeTo(StateID stateID, Consumer<State> action) {
		if (currentStateID == stateID) {
			return;
		}
		Application.Log.info("FSM(" + description + "): " + currentStateID + " -> "  + stateID);
		if (currentStateID != null) {
			state().doExit();
		}
		currentStateID = stateID;
		if (action != null) {
			action.accept(state());
		}
		state().doEntry();
	}

	public void changeTo(StateID stateID) {
		changeTo(stateID, null);
	}
}
