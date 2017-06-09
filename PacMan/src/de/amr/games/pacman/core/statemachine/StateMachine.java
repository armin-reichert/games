package de.amr.games.pacman.core.statemachine;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A finite state machine.
 *
 * @param <StateID>
 *          type for identifying states, for example an enumeration type
 */
public class StateMachine<StateID> {

	private final String description;
	private final Map<StateID, State> statesByID;
	private final Map<StateID, List<StateTransition<StateID>>> transitions;
	private StateID currentStateID;
	private Logger logger;
	private int fps;

	public StateMachine(Map<StateID, State> stateMap) {
		this("Anon state machine", stateMap);
	}

	public StateMachine(String description, Map<StateID, State> stateMap) {
		this.description = description;
		this.statesByID = stateMap;
		this.transitions = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public boolean inState(StateID... stateIDs) {
		for (StateID stateID : stateIDs) {
			if (currentStateID == stateID) {
				return true;
			}
		}
		return false;
	}

	public StateID stateID() {
		return currentStateID;
	}

	public State state() {
		if (currentStateID == null) {
			throw new IllegalStateException("State machine '" + description + "' not initialized");
		}
		return state(currentStateID);
	}

	public State state(StateID stateID) {
		if (!statesByID.containsKey(stateID)) {
			State state = new State();
			statesByID.put(stateID, state);
			if (transitions.containsKey(stateID)) {
				transitions.get(stateID).clear();
			} else {
				transitions.put(stateID, new ArrayList<>());
			}
		}
		return statesByID.get(stateID);
	}

	public State state(StateID stateID, State state) {
		if (statesByID.containsKey(stateID)) {
			throw new IllegalStateException("State with ID '" + stateID + "' already exists");
		}
		statesByID.put(stateID, state);
		if (transitions.containsKey(stateID)) {
			transitions.get(stateID).clear();
		} else {
			transitions.put(stateID, new ArrayList<>());
		}
		return state;
	}

	public void update() {
		StateTransition<StateID> transition = findMatchingTransition();
		if (transition != null) {
			changeTo(transition.newState, transition.action);
		} else {
			state().doUpdate();
		}
	}

	public void change(StateID from, StateID to, BooleanSupplier condition) {
		change(from, to, condition, null);
	};
	
	public void change(StateID from, StateID to, BooleanSupplier condition, Consumer<State> action) {
		StateTransition<StateID> transition = new StateTransition<>();
		transition.oldState = from;
		transition.newState = to;
		transition.condition = condition;
		transition.action = action;
		transitions.get(from).add(transition);
	}

	void changeTo(StateID stateID, Consumer<State> action) {
		if (currentStateID == stateID) {
			return;
		}
		traceStateChange(currentStateID, stateID);
		if (currentStateID != null) {
			state().doExit();
			traceExit();
		}
		currentStateID = stateID;
		if (action != null) {
			action.accept(state());
		}
		traceEntry();
		state().doEntry();
	}

	public void changeTo(StateID stateID) {
		changeTo(stateID, null);
	}

	private StateTransition<StateID> findMatchingTransition() {
		for (StateTransition<StateID> transition : transitions.get(currentStateID)) {
			if (transition.condition.getAsBoolean()) {
				return transition;
			}
		}
		return null;
	}

	// Tracing

	public void setLogger(Logger logger, int fps) {
		this.logger = logger;
		this.fps = fps;
	}

	private void traceStateChange(StateID oldState, StateID newState) {
		if (logger != null) {
			logger.info(format("FSM(%s) changes from '%s' to '%s'", description, oldState, newState));
		}

	}

	private void traceEntry() {
		if (logger != null) {
			if (state().getDuration() != State.FOREVER) {
				float seconds = (float) state().getDuration() / fps;
				logger.info(format("FSM(%s) enters state '%s' for %.2f seconds (%d frames)", description, stateID(), seconds,
						state().getDuration()));
			} else {
				logger.info(format("FSM(%s) enters state '%s'", description, stateID()));
			}
		}
	}

	private void traceExit() {
		if (logger != null) {
			logger.info(format("FSM(%s) exits state '%s'", description, stateID()));
		}
	}
}