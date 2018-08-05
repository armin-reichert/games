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
}