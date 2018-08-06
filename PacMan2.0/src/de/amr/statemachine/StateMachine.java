package de.amr.statemachine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
 *          type for identifying states, for example an enumeration type.
 * @param <E>
 *          type of inputs (events).
 * 
 * @author Armin Reichert
 */
public class StateMachine<S, E> {

	public static <SS, EE> StateMachineBuilder<SS, EE> builder(Class<SS> stateLabelType, Class<EE> eventType) {
		return new StateMachineBuilder<>(stateLabelType);
	}

	class Transition implements StateTransition<S, E> {

		S from;
		S to;
		E event;
		BooleanSupplier guard;
		Consumer<StateTransition<S, E>> action = t -> {
		};
		Class<? extends E> eventType;
		boolean timeout;

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

	/** Function defining how many ticks per second are sent to the machine. */
	public IntSupplier fnPulse = () -> 60;
	String description;
	Class<S> stateLabelType;
	Deque<E> eventQ;
	Map<S, StateObject<S, E>> stateMap;
	Map<S, List<Transition>> transitionsFromState;
	S initialState;
	S currentState;
	StateMachineTracer<S, E> trace;

	/**
	 * Creates a new state machine.
	 * 
	 * @param stateLabelType
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StateMachine(Class<S> stateLabelType) {
		this.eventQ = new ArrayDeque<>();
		this.stateMap = new HashMap<>(); // TODO
		this.transitionsFromState = new HashMap<>();
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
	public boolean any(S... states) {
		if (states.length == 0) {
			throw new IllegalArgumentException("At least one state ID is needed");
		}
		return Stream.of(states).anyMatch(state -> state.equals(currentState));
	}

	/**
	 * @return the current state (identifier)
	 */
	public S currentState() {
		return currentState;
	}

	/**
	 * 
	 * @return the state object of the current state
	 */
	public <C extends StateObject<S, E>> C getStateImpl() {
		return state(currentState);
	}

	/**
	 * Returns the state object with the given identifier. The state object is created on demand.
	 * 
	 * @param state
	 *                a state identifier
	 * @return the state object for the given state identifier
	 */
	@SuppressWarnings("unchecked")
	public <C extends StateObject<S, E>> C state(S state) {
		if (!stateMap.containsKey(state)) {
			StateObject<S, E> stateObject = new StateObject<>();
			stateObject.id = state;
			stateObject.machine = this;
			stateMap.put(state, stateObject);
			return (C) stateObject;
		}
		return (C) stateMap.get(state);
	}

	/**
	 * Initializes this state machine by switching to the initial state and executing the initial
	 * state's (optional) entry action.
	 */
	public void init() {
		trace.enteringInitialState(initialState);
		currentState = initialState;
		state(currentState).resetTimer();
		state(currentState).onEntry();
	}

	/**
	 * Triggers an update (processing step) of this state machine.
	 */
	public void update() {
		E event = eventQ.peek();
		if (event == null) {
			// find transition without event
			Optional<Transition> match = transitionsFrom(currentState).stream().filter(this::canFire).findFirst();
			if (match.isPresent()) {
				fireTransition(match.get(), event);
			} else {
				// perform update for current state
				state(currentState).timerStep();
				state(currentState).onTick();
			}
		} else {
			// find transition for current event
			Optional<Transition> match = transitionsFrom(currentState).stream().filter(this::canFire).findFirst();
			if (match.isPresent()) {
				fireTransition(match.get(), event);
			} else {
				trace.ignoredEvent(event);
				// TODO should we always throw an exception? Maybe make it configurable?
				throw new IllegalStateException(
						String.format("No transition defined in state '%s' for event '%s'", currentState, event));
			}
			eventQ.poll();
		}
	}

	private boolean canFire(Transition t) {
		boolean guardOk = t.guard == null || t.guard.getAsBoolean();
		if (t.timeout) {
			return guardOk && state(t.from).isTerminated();
		} else if (t.eventType != null) {
			return guardOk && hasMatchingEvent(t.eventType);
		} else {
			return guardOk;
		}
	}

	// TODO make this configurable
	private boolean hasMatchingEvent(Class<? extends E> eventType) {
		return !eventQ.isEmpty() && eventQ.peek().getClass().equals(eventType);
	}

	private void fireTransition(Transition t, E event) {
		t.event = event;
		trace.firingTransition(t);
		if (currentState == t.to) {
			// keep state: no exit/entry actions are executed
			if (t.action != null) {
				t.action.accept(t);
			}
		} else {
			// change state, execute exit and entry actions
			StateObject<S, E> oldState = state(t.from);
			StateObject<S, E> newState = state(t.to);
			trace.exitingState(currentState);
			oldState.onExit();
			if (t.action != null) {
				t.action.accept(t);
			}
			currentState = t.to;
			trace.enteringState(t.to);
			newState.resetTimer();
			newState.onEntry();
		}
	}

	private List<Transition> transitionsFrom(S state) {
		if (!transitionsFromState.containsKey(state)) {
			transitionsFromState.put(state, new ArrayList<>(3));
		}
		return transitionsFromState.get(state);
	}

	// Builder calls this

	void addTransition(S from, S to, BooleanSupplier guard, Consumer<StateTransition<S, E>> action,
			Class<? extends E> eventType, boolean timeout) {
		Transition t = new Transition();
		t.from = from;
		t.to = to;
		t.guard = guard;
		t.action = action;
		t.eventType = eventType;
		t.timeout = timeout;
		transitionsFrom(from).add(t);
	}
}