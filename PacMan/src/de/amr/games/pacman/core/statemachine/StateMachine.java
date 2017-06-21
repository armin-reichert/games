package de.amr.games.pacman.core.statemachine;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A finite state machine.
 *
 * @param <StateID>
 *          type for identifying states, for example an enumeration type
 * @param <Input>
 *          type for inputs / events
 */
public class StateMachine<StateID, Input> {

	private final String description;
	private final Map<StateID, State> statesByID;
	private final Map<StateID, List<Transition<StateID>>> transitionsByStateID;
	private final StateID initialStateID;
	private StateID currentStateID;
	private Deque<Input> inputQ = new LinkedList<>();
	private Optional<Logger> logger;
	private int fps;

	public StateMachine(Map<StateID, State> statesByID, StateID initialStateID) {
		this("Anon state machine", statesByID, initialStateID);
	}

	public StateMachine(String description, Map<StateID, State> statesByID, StateID initialStateID) {
		this.description = description;
		this.statesByID = statesByID;
		this.initialStateID = initialStateID;
		this.transitionsByStateID = new HashMap<>();
		this.logger = Optional.empty();
	}

	public Logger getLogger() {
		return logger.get();
	}

	public void setLogger(Logger logger, int fps) {
		this.logger = Optional.of(logger);
		this.fps = fps;
	}

	public void init() {
		enterState(initialStateID, null);
	}

	public void addInput(Input input) {
		inputQ.add(input);
	}

	@SuppressWarnings("unchecked")
	public boolean is(StateID... stateIDs) {
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
		if (!inputQ.isEmpty()) {
			logger.ifPresent(log -> log.info(format("FSM(%s) processes event '%s'", description, inputQ.peek())));
			step();
			inputQ.poll();
		} else {
			step();
		}
	}

	private void step() {
		Optional<Transition<StateID>> match = getOutgoingTransitions(currentStateID).stream()
				.filter(transition -> transition.condition.getAsBoolean()).findFirst();
		if (match.isPresent()) {
			enterState(match.get().newState, match.get().action);
		} else {
			state().doUpdate();
		}
	}

	private void enterState(StateID newStateID, Consumer<State> action) {
		if (currentStateID == newStateID) {
			return;
		}
		traceStateChange(currentStateID, newStateID);
		if (currentStateID != null) {
			state().doExit();
			traceStateExit();
		}
		currentStateID = newStateID;
		if (action != null) {
			action.accept(state());
		}
		traceStateEntry();
		state().doEntry();
	}

	private List<Transition<StateID>> getOutgoingTransitions(StateID stateID) {
		if (!transitionsByStateID.containsKey(stateID)) {
			transitionsByStateID.put(stateID, new ArrayList<>(3));
		}
		return transitionsByStateID.get(stateID);
	}

	// Tracing

	private void traceStateEntry() {
		logger.ifPresent(log -> {
			if (state().getDuration() != State.FOREVER) {
				float seconds = (float) state().getDuration() / fps;
				log.info(format("FSM(%s) enters state '%s' for %.2f seconds (%d frames)", description, stateID(), seconds,
						state().getDuration()));
			} else {
				log.info(format("FSM(%s) enters state '%s'", description, stateID()));
			}
		});
	}

	private void traceStateChange(StateID oldState, StateID newState) {
		logger.ifPresent(log -> log.info(format("FSM(%s) changes from '%s' to '%s'", description, oldState, newState)));
	}

	private void traceStateExit() {
		logger.ifPresent(log -> log.info(format("FSM(%s) exits state '%s'", description, stateID())));
	}

	// methods for specifying state graph

	public void change(StateID from, StateID to, BooleanSupplier condition) {
		change(from, to, condition, null);
	};

	public void changeOnTimeout(StateID from, StateID to) {
		changeOnTimeout(from, to, null);
	}

	public void changeOnTimeout(StateID from, StateID to, Consumer<State> action) {
		change(from, to, () -> state(from).isTerminated(), action);
	}

	public void changeOnInput(Input input, StateID from, StateID to) {
		change(from, to, () -> input.equals(inputQ.peek()));
	}

	public void changeOnInput(Input input, StateID from, StateID to, BooleanSupplier condition) {
		change(from, to, () -> condition.getAsBoolean() && input.equals(inputQ.peek()));
	}
	
	public void changeOnInput(Input input, StateID from, StateID to, Consumer<State> action) {
		change(from, to, () -> input.equals(inputQ.peek()), action);
	}

	public void change(StateID from, StateID to, BooleanSupplier condition, Consumer<State> action) {
		Transition<StateID> transition = new Transition<>();
		transition.oldState = from;
		transition.newState = to;
		transition.condition = condition;
		transition.action = action;
		getOutgoingTransitions(from).add(transition);
	}
}