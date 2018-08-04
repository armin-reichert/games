package de.amr.statemachine;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Implementation of a state in a finite state machine.
 * 
 * @author Armin Reichert
 */
public class StateObject<S, E> {

	/** Constant for defining an unlimited duration. */
	public static final int UNLIMITED = Integer.MAX_VALUE;

	/** The state machine this state belongs to. */
	public final StateMachine<S, E> sm;

	/** The label used to identify this state. */
	public final S id;

	/** The client code executed when entering this state. */
	public Consumer<StateObject<S, E>> entry;

	/** The client code executed when an update occurs for this state. */
	public Consumer<StateObject<S, E>> update;

	/** The client code executed when leaving this state. */
	public Consumer<StateObject<S, E>> exit;

	/** The duration until this state times out. */
	private int duration;

	/** Ticks remaining until time-out */
	private int remaining;

	/**
	 * Creates a new state with unlimited duration.
	 */
	public StateObject(StateMachine<S, E> sm, S label) {
		this.sm = sm;
		this.id = label;
		remaining = duration = UNLIMITED;
		entry = this::onEntry;
		exit = this::onExit;
		update = this::onTick;
		defineTransitions();
	}

	public void onEntry(StateObject<S, E> self) {
	}

	public void onExit(StateObject<S, E> self) {
	}

	public void onTick(StateObject<S, E> self) {
	}

	public void defineTransitions() {
	}

	void doEntry() {
		if (entry != null) {
			entry.accept(this);
		}
	}

	void doExit() {
		if (exit != null) {
			exit.accept(this);
		}
	}

	void doUpdate() {
		if (remaining > 0) {
			--remaining;
		}
		if (update != null) {
			update.accept(this);
		}
	}

	/** Tells if this state has timed out. */
	public boolean isTerminated() {
		return remaining == 0;
	}

	/** Sets the duration of this state and resets the timer. */
	public void setDuration(int ticks) {
		if (ticks < 0) {
			throw new IllegalStateException();
		}
		duration = ticks;
		resetTimer();
	}

	/** Resets the timer to the complete state duration. */
	public void resetTimer() {
		remaining = duration;
	}

	/**
	 * Returns the duration of this state.
	 * 
	 * @return the state duration (number of updates until this state times out)
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Returns the number of updates until this state will time out.
	 * 
	 * @return the number of updates until timeout occurs
	 */
	public int getRemaining() {
		return remaining;
	}

	// Methods for building state graph

	private final BooleanSupplier ALWAYS_TRUE = () -> true;
	private final Consumer<StateTransition<S, E>> NO_ACTION = t -> {
	};

	/**
	 * Defines a transition to the given state guarded by the given condition. If the transition is
	 * executed the given action is executed just before the new state is entered.
	 * 
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    a condition (guard)
	 * @param action
	 *                    code which will be executed when this transition occurs
	 */
	public StateObject<S, E> change(S to, BooleanSupplier condition, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, to, condition, action);
		return this;
	}

	/**
	 * Defines a transition to the given state guarded by the given condition.
	 * 
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    a condition (guard)
	 */
	public StateObject<S, E> change(S to, BooleanSupplier condition) {
		sm.addTransition(id, to, condition, NO_ACTION);
		return this;
	};

	/**
	 * Defines a transition to the given state which can always be fired.
	 * 
	 * @param to
	 *             the target state
	 */
	public StateObject<S, E> change(S to) {
		sm.addTransition(id, to, ALWAYS_TRUE, NO_ACTION);
		return this;
	};

	/**
	 * Defines a transition to the given state which can be fired if this state is timed out.
	 * 
	 * @param to
	 *             the target state
	 */
	public StateObject<S, E> changeOnTimeout(S to) {
		sm.addTransition(id, to, this::isTerminated, NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition to the given state which can be fired if this state is timed out and the
	 * given condition holds.
	 * 
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 */
	public StateObject<S, E> changeOnTimeout(S to, BooleanSupplier condition) {
		sm.addTransition(id, to, () -> isTerminated() && condition.getAsBoolean(), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition to the given state which can be fired if this state is timed out.
	 * 
	 * @param to
	 *                 the target state
	 * @param action
	 *                 code which will be executed when this transition occurs
	 */
	public StateObject<S, E> changeOnTimeout(S to, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, to, this::isTerminated, action);
		return this;
	}

	/**
	 * Defines a transition without state change which can be fired if this state is timed out.
	 * 
	 * @param action
	 *                 code which will be executed when this transition occurs
	 */
	public StateObject<S, E> onTimeout(Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, id, this::isTerminated, action);
		return this;
	}

	/**
	 * Defines a transition to the given state which can be fired if this state is timed out and the
	 * given condition holds.
	 * 
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 * @param action
	 *                    code which will be executed when this transition occurs
	 */
	public StateObject<S, E> changeOnTimeout(S to, BooleanSupplier condition, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, to, () -> isTerminated() && condition.getAsBoolean(), action);
		return this;
	}

	/**
	 * Defines a transition without state change which can be fired if this state is timed out and the
	 * given condition holds.
	 * 
	 * @param condition
	 *                    some condition
	 * @param action
	 *                    code which will be executed when this transition occurs
	 */
	public StateObject<S, E> onTimeout(BooleanSupplier condition, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, id, () -> isTerminated() && condition.getAsBoolean(), action);
		return this;
	}

	/**
	 * Defines a transition to the given state which can be fired if the current event matches the given
	 * type.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 */
	public StateObject<S, E> changeOnInput(Class<? extends E> eventType, S to) {
		sm.addTransition(id, to, () -> sm.hasMatchingEvent(eventType), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition without state change which can be fired if the current event matches the
	 * given type.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 */
	public StateObject<S, E> onInput(Class<? extends E> eventType) {
		sm.addTransition(id, id, () -> sm.hasMatchingEvent(eventType), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition to the given state which can be fired if the current event matches the given
	 * type and the given condition holds.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 */
	public StateObject<S, E> changeOnInput(Class<? extends E> eventType, S to, BooleanSupplier condition) {
		sm.addTransition(id, to, () -> sm.hasMatchingEvent(eventType) && condition.getAsBoolean(), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition without state change which can be fired if the current event matches the
	 * given type and the given condition holds.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 */
	public StateObject<S, E> onInput(Class<? extends E> eventType, BooleanSupplier condition) {
		sm.addTransition(id, id, () -> sm.hasMatchingEvent(eventType) && condition.getAsBoolean(), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition to the given state which can be fired if the current event matches the given
	 * type.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 * @param action
	 *                    performed action
	 */
	public StateObject<S, E> changeOnInput(Class<? extends E> eventType, S to, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, to, () -> sm.hasMatchingEvent(eventType), action);
		return this;
	}

	/**
	 * Defines a transition without state change which can be fired if the current event matches the
	 * given type.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 * @param action
	 *                    performed action
	 */
	public StateObject<S, E> onInput(Class<? extends E> eventType, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, id, () -> sm.hasMatchingEvent(eventType), action);
		return this;
	}

	/**
	 * Defines a transition to the given state which can be fired if the current event matches the given
	 * type and the given condition holds.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 * @param action
	 *                    performed action
	 */
	public StateObject<S, E> changeOnInput(Class<? extends E> eventType, S to, BooleanSupplier condition,
			Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, to, () -> sm.hasMatchingEvent(eventType) && condition.getAsBoolean(), action);
		return this;
	}

	/**
	 * Defines a transition without state change which can be fired if the current event matches the
	 * given type and the given condition holds.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 * @param action
	 *                    performed action
	 */
	public StateObject<S, E> onInput(Class<? extends E> eventType, BooleanSupplier condition,
			Consumer<StateTransition<S, E>> action) {
		sm.addTransition(id, id, () -> sm.hasMatchingEvent(eventType) && condition.getAsBoolean(), action);
		return this;
	}

}