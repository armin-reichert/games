package de.amr.easy.statemachine;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A finite state machine.
 *
 * @param <StateID>
 *          type for identifying states, for example an enumeration type
 * @param <Input>
 *          type for inputs / events
 * 
 * @author Armin Reichert
 */
public class StateMachine<StateID, Input> {

	private final String description;
	private final Map<StateID, State> statesByID;
	private final Map<StateID, List<Transition<StateID>>> transitionsByStateID;
	private final StateID initialStateID;
	private final Deque<Input> inputQ = new LinkedList<>();
	private StateID currentStateID;
	private Optional<Logger> logger;
	private int frequency = 60;

	/**
	 * Creates a new state machine.
	 * 
	 * @param description
	 *          a string describing this state machine, used for tracing
	 * @param stateIDClass
	 *          type used for the state IDs, for example an enum
	 * @param initialStateID
	 *          the ID of the initial state
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StateMachine(String description, Class<StateID> stateIDClass, StateID initialStateID) {
		this.description = description;
		this.statesByID = stateIDClass.isEnum() ? new EnumMap(stateIDClass) : new HashMap<>();
		this.initialStateID = initialStateID;
		this.transitionsByStateID = new HashMap<>();
		this.logger = Optional.empty();
	}

	/**
	 * Sets a logger and activates tracing to this logger.
	 * 
	 * @param log
	 *          a logger
	 */
	public void setLogger(Logger log) {
		this.logger = log != null ? Optional.of(log) : Optional.empty();
	}

	/**
	 * Sets the frequency with which this state machine is updated per second. Used for tracing.
	 * 
	 * @param frequency
	 *          the frequency (ticks per second) how this state machine is updated
	 * 
	 *          TODO this does not really belong to such a general class
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	/**
	 * The description of this state machine.
	 * 
	 * @return the description of this state machine
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Initializes this state machine by switching to the initial state and executing an optional
	 * entry action.
	 */
	public void init() {
		currentStateID = initialStateID;
		traceStateEntry();
		state().doEntry();
	}

	/**
	 * Adds an input ("event") to this state machine which will be picked in the next processing step.
	 * 
	 * @param input
	 *          some input / event
	 */
	public void addInput(Input input) {
		inputQ.add(input);
	}

	/**
	 * Tells if there is input.
	 * 
	 * @return if there is input
	 */
	public boolean hasInput() {
		return !inputQ.isEmpty();
	}

	/**
	 * Tells if the state machine is in any of the given states.
	 * 
	 * @param stateIDs
	 *          non-empty list of state IDs
	 * @return <code>true</code> if the state machine is in any of the given states
	 */
	@SuppressWarnings("unchecked")
	public boolean is(StateID... stateIDs) {
		if (stateIDs.length == 0) {
			throw new IllegalArgumentException("At least one state ID is needed");
		}
		return Stream.of(stateIDs).anyMatch(stateID -> stateID == currentStateID);
	}

	/**
	 * Returns the ID of the current state.
	 * 
	 * @return the current state's ID
	 */
	public StateID stateID() {
		return currentStateID;
	}

	/**
	 * Returns the current state object. This is needed for example to set the duration of a timed
	 * state.
	 * 
	 * @return the current state object
	 */
	public State state() {
		if (currentStateID == null) {
			throw new IllegalStateException("State machine '" + description + "' not initialized");
		}
		return state(currentStateID);
	}

	/**
	 * Returns the state with the given ID. If this state is accessed for the first time, it will be
	 * created.
	 * 
	 * @param stateID
	 *          a state ID
	 * @return the state object for the given ID
	 */
	public State state(StateID stateID) {
		if (!statesByID.containsKey(stateID)) {
			statesByID.put(stateID, new State());
			logger.ifPresent(log -> log.info(format("Created state %s:%s ", description, stateID)));
		}
		return statesByID.get(stateID);
	}

	/**
	 * Defines the state with the given ID by the given state object which may be an instance of a
	 * specialized state subclass. This is useful if the state object itself needs "state".
	 * 
	 * @param stateID
	 *          some state ID
	 * @param state
	 *          a state object (maybe of a subclass of State)
	 * @return the state object
	 */
	public State defineState(StateID stateID, State state) {
		if (statesByID.containsKey(stateID)) {
			throw new IllegalStateException("State with ID '" + stateID + "' already exists");
		}
		statesByID.put(stateID, state);
		return state;
	}

	/**
	 * Triggers an update (processing step) of this state machine.
	 */
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
			enterState(match.get().to, match.get().action);
		} else {
			state().doUpdate();
		}
	}

	private void enterState(StateID newStateID, BiConsumer<State, State> action) {
		if (currentStateID == newStateID) {
			if (action != null) {
				action.accept(state(), state());
			}
			return; // state loop, no exit/entry actions are executed
		}
		traceStateChange(currentStateID, newStateID);
		if (currentStateID != null) {
			state().doExit();
			traceStateExit();
		}
		State stateBefore = state(currentStateID);
		currentStateID = newStateID;
		if (action != null) {
			action.accept(stateBefore, state());
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
				float seconds = (float) state().getDuration() / frequency;
				log.info(format("FSM(%s) enters state '%s' for %.2f seconds (%d frames)", description, stateID(), seconds,
						state().getDuration()));
			} else {
				log.info(format("FSM(%s) enters state '%s'", description, stateID()));
			}
		});
	}

	private void traceStateChange(StateID oldState, StateID newState) {
		logger.ifPresent(log -> {
			if (oldState != newState) {
				log.info(format("FSM(%s) changes from '%s' to '%s'", description, oldState, newState));
			} else {
				log.info(format("FSM(%s) stays in state '%s'", description, oldState));
			}
		});
	}

	private void traceStateExit() {
		logger.ifPresent(log -> log.info(format("FSM(%s) exits state '%s'", description, stateID())));
	}

	// methods for specifying the transitions

	/**
	 * Defines a transition between the given states which can be fired only if the given condition
	 * holds. If the transition is executed the given action is also executed. This happens before the
	 * new state is entered.
	 * 
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param condition
	 *          the condition which must hold
	 * @param action
	 *          the action executed when the transition is executed
	 */
	public void change(StateID from, StateID to, BooleanSupplier condition, BiConsumer<State, State> action) {
		Transition<StateID> transition = new Transition<>();
		transition.from = from;
		transition.to = to;
		transition.condition = condition;
		transition.action = action;
		getOutgoingTransitions(from).add(transition);
	}

	/**
	 * Defines a transition between the given states which can be fired only if the given condition
	 * holds.
	 * 
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param condition
	 *          the condition which must hold
	 */
	public void change(StateID from, StateID to, BooleanSupplier condition) {
		change(from, to, condition, null);
	};

	/**
	 * Defines a transition between the given states which can always be fired.
	 * 
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 */
	public void change(StateID from, StateID to) {
		change(from, to, () -> true);
	};

	/**
	 * Defines a transition between the given states which can be fired if the source state got a
	 * timeout.
	 * 
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 */
	public void changeOnTimeout(StateID from, StateID to) {
		changeOnTimeout(from, to, null);
	}

	/**
	 * Defines a transition between the given states which can be fired if the source state got a
	 * timeout.
	 * 
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param action
	 *          the action executed when the transition is executed
	 */
	public void changeOnTimeout(StateID from, StateID to, BiConsumer<State, State> action) {
		change(from, to, () -> state(from).isTerminated(), action);
	}

	/**
	 * Defines a transition between the given states which can be fired only if the given input
	 * (event) equals the given input (event).
	 * 
	 * @param input
	 *          the current input (event)
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 */
	public void changeOnInput(Input input, StateID from, StateID to) {
		change(from, to, () -> input.equals(inputQ.peek()));
	}

	/**
	 * Defines a transition between the given states which can be fired only if the given input
	 * (event) equals the current input (event) and the given condition holds.
	 * 
	 * @param input
	 *          the current input (event)
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param condition
	 *          some condition
	 */
	public void changeOnInput(Input input, StateID from, StateID to, BooleanSupplier condition) {
		change(from, to, () -> condition.getAsBoolean() && input.equals(inputQ.peek()));
	}

	/**
	 * Defines a transition between the given states which can be fired only if the given input
	 * (event) equals the current input (event). If the transition fires, the given action is
	 * executed.
	 * 
	 * @param input
	 *          the current input (event)
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param action
	 *          some action method. When calling the action, the first parameter contains the old
	 *          state and the second parameter the new state after the transition has fired
	 */
	public void changeOnInput(Input input, StateID from, StateID to, BiConsumer<State, State> action) {
		change(from, to, () -> input.equals(inputQ.peek()), action);
	}

	/**
	 * Defines a transition between the given states which can be fired only if the given input
	 * (event) equals the current input (event). If the transition fires, the given action is
	 * executed.
	 * 
	 * @param input
	 *          the current input (event)
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param condition
	 *          some condition
	 * @param action
	 *          some action method. When calling the action, the first parameter contains the old
	 *          state and the second parameter the new state after the transition has fired
	 */
	public void changeOnInput(Input input, StateID from, StateID to, BooleanSupplier condition,
			BiConsumer<State, State> action) {
		change(from, to, () -> condition.getAsBoolean() && input.equals(inputQ.peek()), action);
	}

}