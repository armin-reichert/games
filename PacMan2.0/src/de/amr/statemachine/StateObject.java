package de.amr.statemachine;

import java.util.function.Consumer;

/**
 * A state of a finite state machine.
 * 
 * @author Armin Reichert
 */
public class StateObject {

	/** Constant for defining an unlimited duration. */
	public static final int FOREVER = Integer.MAX_VALUE;

	/** The action performed when entering this state. */
	public Consumer<StateObject> entry;

	/** The action performed when an update occurs during this state. */
	public Consumer<StateObject> update;

	/** The action performed when leaving this state. */
	public Consumer<StateObject> exit;

	/** The duration until this state terminates. */
	private int duration;

	/** Ticks remaining until time-out */
	private int remaining;

	/**
	 * Creates a new state with unlimited duration.
	 */
	public StateObject() {
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
}