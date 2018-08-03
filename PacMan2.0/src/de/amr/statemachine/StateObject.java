package de.amr.statemachine;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A state of a finite state machine.
 * 
 * @author Armin Reichert
 */
public class StateObject<S, E> {

	/** Constant for defining an unlimited duration. */
	public static final int FOREVER = Integer.MAX_VALUE;

	public final StateMachine<S, E> sm;

	public final S label;

	/** The action performed when entering this state. */
	public Consumer<StateObject<S, E>> entry;

	/** The action performed when an update occurs during this state. */
	public Consumer<StateObject<S, E>> update;

	/** The action performed when leaving this state. */
	public Consumer<StateObject<S, E>> exit;

	/** The duration until this state terminates. */
	private int duration;

	/** Ticks remaining until time-out */
	private int remaining;

	/**
	 * Creates a new state with unlimited duration.
	 */
	public StateObject(StateMachine<S, E> sm, S label) {
		this.sm = sm;
		this.label = label;
		duration = FOREVER;
		resetTimer();
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
	public void setDuration(int updates) {
		if (updates < 0) {
			throw new IllegalStateException();
		}
		duration = updates;
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
	 * Defines a transition between the given states which can be fired only if the given condition
	 * holds. If the transition is executed the given action is also executed. This happens before the
	 * new state is entered.
	 * 
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    the condition which must hold
	 * @param action
	 *                    code which will be executed when this transition occurs
	 */
	public StateObject<S, E> change(S to, BooleanSupplier condition, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(label, to, condition, action);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired only if the given condition
	 * holds.
	 * 
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    the condition which must hold
	 */
	public StateObject<S, E> change(S to, BooleanSupplier condition) {
		sm.addTransition(label, to, condition, NO_ACTION);
		return this;
	};

	/**
	 * Defines a transition between the given states which can always be fired.
	 * 
	 * @param to
	 *             the target state
	 */
	public StateObject<S, E> change(S to) {
		sm.addTransition(label, to, ALWAYS_TRUE, NO_ACTION);
		return this;
	};

	/**
	 * Defines a transition between the given states which can be fired if the source state got a
	 * timeout.
	 * 
	 * @param to
	 *             the target state
	 */
	public StateObject<S, E> changeOnTimeout(S to) {
		sm.addTransition(label, to, () -> isTerminated(), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired if the source state got a
	 * timeout and the given condition holds.
	 * 
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 */
	public StateObject<S, E> changeOnTimeout(S to, BooleanSupplier condition) {
		sm.addTransition(label, to, () -> isTerminated() && condition.getAsBoolean(), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired if the source state got a
	 * timeout.
	 * 
	 * @param to
	 *                 the target state
	 * @param action
	 *                 code which will be executed when this transition occurs
	 */
	public StateObject<S, E> changeOnTimeout(S to, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(label, to, this::isTerminated, action);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired if this state got a timeout.
	 * 
	 * @param action
	 *                 code which will be executed when this transition occurs
	 */
	public StateObject<S, E> onTimeout(Consumer<StateTransition<S, E>> action) {
		sm.addTransition(label, label, this::isTerminated, action);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired if the source state got a
	 * timeout and the given condition holds.
	 * 
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 * @param action
	 *                    code which will be executed when this transition occurs
	 */
	public StateObject<S, E> changeOnTimeout(S to, BooleanSupplier condition, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(label, to, () -> isTerminated() && condition.getAsBoolean(), action);
		return this;
	}

	/**
	 * Defines a transition in this state which can be fired if this state got a timeout and the given
	 * condition holds.
	 * 
	 * @param condition
	 *                    some condition
	 * @param action
	 *                    code which will be executed when this transition occurs
	 */
	public StateObject<S, E> onTimeout(BooleanSupplier condition, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(label, label, () -> isTerminated() && condition.getAsBoolean(), action);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired only if the next event matches
	 * the transition.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 */
	public StateObject<S, E> changeOnInput(Class<? extends E> eventType, S to) {
		sm.addTransition(label, to, () -> sm.isEventMatching(eventType), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition in this state which can be fired only if the next event matches the
	 * transition.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 */
	public StateObject<S, E> onInput(Class<? extends E> eventType) {
		sm.addTransition(label, label, () -> sm.isEventMatching(eventType), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired only if the next event matches
	 * the transition and the given condition holds.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 * @param condition
	 *                    some condition
	 */
	public StateObject<S, E> changeOnInput(Class<? extends E> eventType, S to, BooleanSupplier condition) {
		sm.addTransition(label, to, () -> sm.isEventMatching(eventType) && condition.getAsBoolean(), NO_ACTION);
		return this;
	}

	public StateObject<S, E> onInput(Class<? extends E> eventType, BooleanSupplier condition) {
		sm.addTransition(label, label, () -> sm.isEventMatching(eventType) && condition.getAsBoolean(), NO_ACTION);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired only if the next event matches
	 * the transition.
	 * 
	 * @param eventType
	 *                    type of events matching the transition
	 * @param to
	 *                    the target state
	 * @param action
	 *                    performed action
	 */
	public StateObject<S, E> changeOnInput(Class<? extends E> eventType, S to, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(label, to, () -> sm.isEventMatching(eventType), action);
		return this;
	}

	public StateObject<S, E> onInput(Class<? extends E> eventType, Consumer<StateTransition<S, E>> action) {
		sm.addTransition(label, label, () -> sm.isEventMatching(eventType), action);
		return this;
	}

	/**
	 * Defines a transition between the given states which can be fired only if the next event matches
	 * the transition.
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
		sm.addTransition(label, to, () -> sm.isEventMatching(eventType) && condition.getAsBoolean(), action);
		return this;
	}

	public StateObject<S, E> onInput(Class<? extends E> eventType, BooleanSupplier condition,
			Consumer<StateTransition<S, E>> action) {
		sm.addTransition(label, label, () -> sm.isEventMatching(eventType) && condition.getAsBoolean(), action);
		return this;
	}

}