package de.amr.statemachine;

import java.util.function.Consumer;

/**
 * Implementation of a state in a finite state machine.
 * 
 * @author Armin Reichert
 */
public class StateObject<S, E> {

	/** Constant for defining an unlimited duration. */
	public static final int UNLIMITED = Integer.MAX_VALUE;

	/** The label used to identify this state. */
	public final S id;

	/** The state machine this state belongs to. */
	final StateMachine<S, E> sm;

	/** The client code executed when entering this state. */
	Consumer<StateObject<S, E>> entry;

	/** The client code executed when an update occurs for this state. */
	Consumer<StateObject<S, E>> update;

	/** The client code executed when leaving this state. */
	Consumer<StateObject<S, E>> exit;

	/** The duration until this state times out. */
	int duration;

	/** Ticks remaining until time-out */
	int remaining;

	/**
	 * Creates a new state with unlimited duration.
	 */
	public StateObject(StateMachine<S, E> sm, S label) {
		this.sm = sm;
		this.id = label;
		remaining = duration = UNLIMITED;
		defineTransitions();
	}

	public void onEntry(StateObject<S, E> self) {
		if (entry != null) {
			entry.accept(this);
		}
	}

	public void onExit(StateObject<S, E> self) {
		if (exit != null) {
			exit.accept(this);
		}
	}

	public void onTick(StateObject<S, E> self) {
		if (update != null) {
			update.accept(this);
		}
	}

	public void defineTransitions() {
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
}