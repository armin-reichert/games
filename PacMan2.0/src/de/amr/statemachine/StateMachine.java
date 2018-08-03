package de.amr.statemachine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A finite state machine.
 *
 * @param <S>
 *          type of state labels, for example an enumeration type.
 * @param <E>
 *          type of inputs (events).
 * 
 * @author Armin Reichert
 */
public class StateMachine<S, E> {

	private class Transition implements StateTransition<S, E> {

		private S from;
		private S to;
		private E event;
		private BooleanSupplier condition;
		private Consumer<StateTransition<S, E>> action = t -> {

		};

		@Override
		public StateObject<S, E> from() {
			return state(from);
		}

		@Override
		public StateObject<S, E> to() {
			return state(to);
		}

		@Override
		public Optional<E> event() {
			return Optional.ofNullable(event);
		}
	}

	public IntSupplier fnPulse = () -> 60;
	private final String description;
	private final Deque<E> eventQ;
	private final Map<S, StateObject<S, E>> stateMap;
	private final Map<S, List<Transition>> transitionMap;
	private final S initialState;
	private S currentState;
	private StateMachineTracer<S, E> trace;

	/**
	 * Creates a new state machine.
	 * 
	 * @param description
	 *                         a string describing this state machine, used for logging
	 * @param stateLabelType
	 *                         type used for labeling the states, for example an enumeration type
	 * @param initialState
	 *                         the label of the initial state
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StateMachine(String description, Class<S> stateLabelType, S initialState) {
		this.description = description;
		this.eventQ = new ArrayDeque<>();
		this.stateMap = stateLabelType.isEnum() ? new EnumMap(stateLabelType) : new HashMap<>();
		this.transitionMap = new HashMap<>();
		this.initialState = initialState;
		this.trace = new StateMachineTracer(this);
	}

	/**
	 * Sets a logger.
	 * 
	 * @param log
	 *              a logger
	 */
	public void setLogger(Logger log) {
		trace.setLog(log);
	}

	/**
	 * @return the description of this state machine
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Adds an input ("event") to the queue of this state machine.
	 * 
	 * @param event
	 *                some input/event
	 */
	public void enqueue(E event) {
		eventQ.add(event);
	}

	/**
	 * Tells if the state machine is in any of the given states.
	 * 
	 * @param states
	 *                 non-empty list of state labels
	 * @return <code>true</code> if the state machine is in any of the given states
	 */
	@SuppressWarnings("unchecked")
	public boolean inOneOf(S... states) {
		if (states.length == 0) {
			throw new IllegalArgumentException("At least one state ID is needed");
		}
		return Stream.of(states).anyMatch(state -> state.equals(currentState));
	}

	/**
	 * @param state
	 *                a state object
	 * @return the label of the given state object
	 */
	public Optional<S> stateLabel(StateObject<S, E> state) {
		return stateMap.entrySet().stream().filter(entry -> entry.getValue().equals(state)).map(Map.Entry::getKey)
				.findFirst();
	}

	/**
	 * @return the current state's label
	 */
	public S currentState() {
		return currentState;
	}

	/**
	 * Returns the state object with the given label. The state object is created on demand.
	 * 
	 * @param stateLabel
	 *                     a state label
	 * @return the state object for the given label
	 */
	public StateObject<S, E> state(S stateLabel) {
		if (!stateMap.containsKey(stateLabel)) {
			stateMap.put(stateLabel, new StateObject<>(this, stateLabel));
			trace.stateCreated(stateLabel);
		}
		return stateMap.get(stateLabel);
	}

	/**
	 * Initializes this state machine by switching to the initial state and executing an optional entry
	 * action.
	 */
	public void init() {
		trace.enteringInitialState(initialState);
		currentState = initialState;
		state(currentState).doEntry();
	}

	/**
	 * Triggers an update (processing step) of this state machine.
	 */
	public void update() {
		E event = eventQ.peek();
		Optional<Transition> match = transitions(currentState).stream().filter(t -> t.condition.getAsBoolean()).findFirst();
		if (match.isPresent()) {
			Transition t = match.get();
			fireTransition(t, event);
		} else {
			if (event != null) {
				trace.ignoredEvent(event);
			}
			state(currentState).doUpdate();
		}
		if (event != null) {
			eventQ.poll();
		}
	}

	boolean isEventMatching(Class<? extends E> eventType) {
		return !eventQ.isEmpty() && eventQ.peek().getClass().equals(eventType);
	}

	private void fireTransition(Transition transition, E event) {
		transition.event = event;
		trace.transition(event, transition.from, transition.to);
		if (currentState == transition.to) {
			// keep state: no exit/entry actions are executed
			transition.action.accept(transition);
		} else {
			// change state, execute exit and entry actions
			if (currentState != null) {
				trace.exitingState(currentState);
				state(currentState).doExit();
			}
			transition.action.accept(transition);
			currentState = transition.to;
			trace.enteringState(currentState);
			state(currentState).doEntry();
		}
	}

	private List<Transition> transitions(S stateLabel) {
		if (!transitionMap.containsKey(stateLabel)) {
			transitionMap.put(stateLabel, new ArrayList<>(3));
		}
		return transitionMap.get(stateLabel);
	}

	void addTransition(S from, S to, BooleanSupplier condition, Consumer<StateTransition<S, E>> action) {
		Transition transition = new Transition();
		transition.from = from;
		transition.to = to;
		transition.condition = condition;
		transition.action = action;
		transitions(from).add(transition);
	}
}