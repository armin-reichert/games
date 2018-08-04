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
import java.util.function.Supplier;
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

	class Transition implements StateTransition<S, E> {

		S from;
		S to;
		E event;
		BooleanSupplier condition;
		Consumer<StateTransition<S, E>> action = t -> {

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

	/** Function defining how many ticks per second are sent to the machine. */
	public IntSupplier fnPulse = () -> 60;

	private final String description;
	private final Deque<E> eventQ;
	private final Map<S, StateObject<S, E>> stateMap;
	private final Map<S, List<Transition>> transitionsFromState;
	private final S initialState;
	private S currentState;
	private StateMachineTracer<S, E> trace;

	/**
	 * Creates a new state machine.
	 * 
	 * @param description
	 *                         a string describing this state machine, used for tracing
	 * @param stateLabelType
	 *                         type used for identifying the states, for example an enumeration type
	 * @param initialState
	 *                         the label of the initial state
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StateMachine(String description, Class<S> stateLabelType, S initialState) {
		this.description = description;
		this.eventQ = new ArrayDeque<>();
		this.stateMap = stateLabelType.isEnum() ? new EnumMap(stateLabelType) : new HashMap<>();
		this.transitionsFromState = new HashMap<>();
		this.initialState = initialState;
		this.currentState = initialState;
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
	 * @return the current state (identifier)
	 */
	public S currentState() {
		return currentState;
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
			stateMap.put(state, new StateObject<>(this, state));
			trace.stateCreated(state);
		}
		return (C) stateMap.get(state);
	}

	/**
	 * Realizes this state by the given custom state.
	 * 
	 * @param state
	 *                              state identifer
	 * @param customStateSupplier
	 *                              custom state constructor function
	 */
	public <C extends CustomStateObject<S, E>> C createState(S state, Supplier<C> customStateSupplier) {
		C customState = customStateSupplier.get();
		stateMap.put(state, customState);
		return customState;
	}

	/**
	 * Initializes this state machine by switching to the initial state and executing the initial
	 * state's (optional) entry action.
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
		if (event == null) {
			// find transition without event
			Optional<Transition> match = transitionsFrom(currentState).stream().filter(t -> t.condition.getAsBoolean())
					.findFirst();
			if (match.isPresent()) {
				fireTransition(match.get(), event);
			} else {
				// perform update for current state
				state(currentState).doUpdate();
			}
		} else {
			// find transition for current event
			Optional<Transition> match = transitionsFrom(currentState).stream().filter(t -> t.condition.getAsBoolean())
					.findFirst();
			if (match.isPresent()) {
				fireTransition(match.get(), event);
			} else {
				trace.ignoredEvent(event); // TODO should we throw an exception in this case? Maybe configurable?
			}
			eventQ.poll();
		}
	}

	// TODO make this configurable
	boolean hasMatchingEvent(Class<? extends E> eventType) {
		return !eventQ.isEmpty() && eventQ.peek().getClass().equals(eventType);
	}

	private void fireTransition(Transition transition, E event) {
		transition.event = event;
		trace.firingTransition(transition);
		if (currentState == transition.to) {
			// keep state: no exit/entry actions are executed
			transition.action.accept(transition);
		} else {
			// change state, execute exit and entry actions
			trace.exitingState(currentState);
			state(currentState).doExit();
			transition.action.accept(transition);
			currentState = transition.to;
			trace.enteringState(transition.to);
			state(currentState).doEntry();
		}
	}

	private List<Transition> transitionsFrom(S state) {
		if (!transitionsFromState.containsKey(state)) {
			transitionsFromState.put(state, new ArrayList<>(3));
		}
		return transitionsFromState.get(state);
	}

	void addTransition(S from, S to, BooleanSupplier condition, Consumer<StateTransition<S, E>> action) {
		Transition transition = new Transition();
		transition.from = from;
		transition.to = to;
		transition.condition = condition;
		transition.action = action;
		transitionsFrom(from).add(transition);
	}
}