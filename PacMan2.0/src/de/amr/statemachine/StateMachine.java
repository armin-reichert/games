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
import java.util.function.Supplier;
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
		private Consumer<StateTransition<S, E>> action;

		@Override
		public State from() {
			return state(from);
		}

		@Override
		public State to() {
			return state(to);
		}

		@Override
		public Optional<E> event() {
			return Optional.ofNullable(event);
		}
	}

	private final String description;
	private final Deque<E> inputQ;
	private final Map<S, State> stateMap;
	private final Map<S, List<Transition>> transitionMap;
	private final S initialStateLabel;

	private S currentStateLabel;
	private int pauseTime;
	private Logger logger;

	public Supplier<Integer> fnFrequency = () -> 60;

	/**
	 * Creates a new state machine.
	 * 
	 * @param description
	 *          a string describing this state machine, used for logging
	 * @param stateLabelType
	 *          type used for labeling the states, for example an enumeration type
	 * @param initialStateLabel
	 *          the label of the initial state
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StateMachine(String description, Class<S> stateLabelType, S initialStateLabel) {
		this.description = description;
		this.inputQ = new ArrayDeque<>();
		this.stateMap = stateLabelType.isEnum() ? new EnumMap(stateLabelType) : new HashMap<>();
		this.transitionMap = new HashMap<>();
		this.initialStateLabel = initialStateLabel;
	}

	/**
	 * Sets a logger.
	 * 
	 * @param logger
	 *          a logger
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * @return (optional) current logger
	 */
	public Optional<Logger> getLogger() {
		return Optional.ofNullable(logger);
	}

	/**
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
		currentStateLabel = initialStateLabel;
		traceStateEntry();
		state().doEntry();
	}

	/**
	 * Adds an input ("event") to the queue of this state machine.
	 * 
	 * @param input
	 *          some input/event
	 */
	public void enqueue(E input) {
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
	 * @param stateLabels
	 *          non-empty list of state labels
	 * @return <code>true</code> if the state machine is in any of the given states
	 */
	@SuppressWarnings("unchecked")
	public boolean is(S... stateLabels) {
		if (stateLabels.length == 0) {
			throw new IllegalArgumentException("At least one state ID is needed");
		}
		return Stream.of(stateLabels).anyMatch(stateLabel -> stateLabel == currentStateLabel);
	}

	/**
	 * @param state
	 *          a state object
	 * @return the label of the given state object
	 */
	public Optional<S> label(State state) {
		return stateMap.entrySet().stream().filter(e -> e.getValue() == state).map(Map.Entry::getKey)
				.findFirst();
	}

	/**
	 * @return the current state's label
	 */
	public S currentStateLabel() {
		return currentStateLabel;
	}

	/**
	 * Returns the current state object. This is needed for example to set the duration of a timed
	 * state.
	 * 
	 * @return the current state object
	 */
	public State state() {
		if (currentStateLabel == null) {
			throw new IllegalStateException("State machine '" + description + "' not initialized");
		}
		return state(currentStateLabel);
	}

	/**
	 * Returns the state object with the given label. The state object is created on demand.
	 * 
	 * @param stateLabel
	 *          a state label
	 * @return the state object for the given label
	 */
	public State state(S stateLabel) {
		if (!stateMap.containsKey(stateLabel)) {
			stateMap.put(stateLabel, new State());
			getLogger().ifPresent(
					log -> log.info(String.format("Created state %s:%s ", description, stateLabel)));
		}
		return stateMap.get(stateLabel);
	}

	/**
	 * Set pause timer for given number of ticks.
	 * 
	 * @param ticks
	 *          number of ticks the state machine should pause updates
	 */
	public void pause(int ticks) {
		if (pauseTime < 0) {
			throw new IllegalArgumentException("Negative pause time is not allowed");
		}
		pauseTime = ticks;
	}

	/**
	 * Triggers an update (processing step) of this state machine.
	 */
	public void update() {
		if (pauseTime > 0) {
			pauseTime -= 1;
			return;
		}
		if (!inputQ.isEmpty()) {
			processInput(inputQ.peek());
			inputQ.poll();
		} else {
			processInput(null);
		}
	}

	private boolean hasMatchingInput(Class<? extends E> inputClass) {
		return hasInput() && inputQ.peek().getClass().equals(inputClass);
	}

	private void processInput(E input) {
		Optional<Transition> match = transitions(currentStateLabel).stream()
				.filter(t -> t.condition.getAsBoolean()).findFirst();
		if (match.isPresent()) {
			Transition t = match.get();
			processTransition(t, input);
		} else {
			if (input != null) {
				getLogger().ifPresent(log -> log
						.info(String.format("Input %s ignored. No matching transition was found", input)));
			}
			state().doUpdate();
		}
	}

	private void processTransition(Transition transition, E input) {
		transition.event = input;
		if (currentStateLabel == transition.to) {
			// state loop, no exit/entry actions are executed
			if (transition.action != null) {
				transition.action.accept(transition);
			}
		} else {
			// state transition into other state, exit/entry actions are executed
			traceStateChange(currentStateLabel, transition.to);
			if (currentStateLabel != null) {
				state().doExit();
				traceStateExit();
			}
			if (transition.action != null) {
				transition.action.accept(transition);
			}
			currentStateLabel = transition.to;
			traceStateEntry();
			state().doEntry();
		}
		getLogger().ifPresent(log -> {
			if (input != null) {
				traceInputStateChange(input, transition.from, transition.to);
			} else {
				traceStateChange(transition.from, transition.to);
			}
		});
	}

	private List<Transition> transitions(S stateLabel) {
		if (!transitionMap.containsKey(stateLabel)) {
			transitionMap.put(stateLabel, new ArrayList<>(3));
		}
		return transitionMap.get(stateLabel);
	}

	// Tracing

	private void traceStateEntry() {
		getLogger().ifPresent(log -> {
			if (state().getDuration() != State.FOREVER) {
				float seconds = state().getDuration() / fnFrequency.get();
				log.info(String.format("FSM(%s) enters state '%s' for %.2f seconds (%d frames)",
						description, currentStateLabel(), seconds, state().getDuration()));
			} else {
				log.info(String.format("FSM(%s) enters state '%s'", description, currentStateLabel()));
			}
		});
	}

	private void traceStateChange(S oldState, S newState) {
		getLogger().ifPresent(log -> {
			if (oldState != newState) {
				log.info(
						String.format("FSM(%s) changes from '%s' to '%s'", description, oldState, newState));
			} else {
				log.info(String.format("FSM(%s) stays in state '%s'", description, oldState));
			}
		});
	}

	private void traceInputStateChange(E input, S oldState, S newState) {
		getLogger().ifPresent(log -> {
			if (oldState != newState) {
				log.info(String.format("FSM(%s) processes %s and changes from '%s' to '%s'", description,
						input, oldState, newState));
			} else {
				log.info(String.format("FSM(%s) processes %s and stays in state '%s'", description, input,
						oldState));
			}
		});
	}

	private void traceStateExit() {
		getLogger().ifPresent(log -> log
				.info(String.format("FSM(%s) exits state '%s'", description, currentStateLabel())));
	}

	// methods for specifying the transitions

	private void addTransition(S from, S to, BooleanSupplier condition,
			Consumer<StateTransition<S, E>> action) {
		Transition transition = new Transition();
		transition.from = from;
		transition.to = to;
		transition.condition = condition;
		transition.action = action;
		transitions(from).add(transition);
	}

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
	 *          code which will be executed when this transition occurs
	 */
	public void change(S from, S to, BooleanSupplier condition,
			Consumer<StateTransition<S, E>> action) {
		addTransition(from, to, condition, action);
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
	public void change(S from, S to, BooleanSupplier condition) {
		addTransition(from, to, condition, (Consumer<StateTransition<S, E>>) null);
	};

	/**
	 * Defines a transition between the given states which can always be fired.
	 * 
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 */
	public void change(S from, S to) {
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
	public void changeOnTimeout(S from, S to) {
		changeOnTimeout(from, to, t -> {
		});
	}

	/**
	 * Defines a transition between the given states which can be fired if the source state got a
	 * timeout and the given condition holds.
	 * 
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param condition
	 *          some condition
	 */
	public void changeOnTimeout(S from, S to, BooleanSupplier condition) {
		changeOnTimeout(from, to, condition, t -> {
		});
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
	 *          code which will be executed when this transition occurs
	 */
	public void changeOnTimeout(S from, S to, Consumer<StateTransition<S, E>> action) {
		addTransition(from, to, () -> state(from).isTerminated(), action);
	}

	/**
	 * Defines a transition between the given states which can be fired if the source state got a
	 * timeout and the given condition holds.
	 * 
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param condition
	 *          some condition
	 * @param action
	 *          code which will be executed when this transition occurs
	 */
	public void changeOnTimeout(S from, S to, BooleanSupplier condition,
			Consumer<StateTransition<S, E>> action) {
		addTransition(from, to, () -> state(from).isTerminated() && condition.getAsBoolean(), action);
	}

	/**
	 * Defines a transition between the given states which can be fired only if the next event matches
	 * the transition.
	 * 
	 * @param eventType
	 *          type of events matching the transition
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 */
	public void changeOnInput(Class<? extends E> eventType, S from, S to) {
		change(from, to, () -> hasMatchingInput(eventType));
	}

	/**
	 * Defines a transition between the given states which can be fired only if the next event matches
	 * the transition and the given condition holds.
	 * 
	 * @param eventType
	 *          type of events matching the transition
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param condition
	 *          some condition
	 */
	public void changeOnInput(Class<? extends E> eventType, S from, S to, BooleanSupplier condition) {
		change(from, to, () -> hasMatchingInput(eventType) && condition.getAsBoolean());
	}

	/**
	 * Defines a transition between the given states which can be fired only if the next event matches
	 * the transition.
	 * 
	 * @param eventType
	 *          type of events matching the transition
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param action
	 *          performed action
	 */
	public void changeOnInput(Class<? extends E> eventType, S from, S to,
			Consumer<StateTransition<S, E>> action) {
		addTransition(from, to, () -> hasMatchingInput(eventType), action);
	}

	/**
	 * Defines a transition between the given states which can be fired only if the next event matches
	 * the transition.
	 * 
	 * @param eventType
	 *          type of events matching the transition
	 * @param from
	 *          the source state
	 * @param to
	 *          the target state
	 * @param condition
	 *          some condition
	 * @param action
	 *          performed action
	 */
	public void changeOnInput(Class<? extends E> eventType, S from, S to, BooleanSupplier condition,
			Consumer<StateTransition<S, E>> action) {
		addTransition(from, to, () -> hasMatchingInput(eventType) && condition.getAsBoolean(), action);
	}
}