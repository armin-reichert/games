package de.amr.games.pacman.core.statemachine;

import java.util.function.Consumer;

/**
 * A state of a finite state machine.
 */
public class State {

	public static final int FOREVER = Integer.MAX_VALUE;

	/** The action performed when entering this state. */
	public Consumer<State> entry;

	/** The action performed when in this state. */
	public Consumer<State> update;

	/** The action performed when exiting this state. */
	public Consumer<State> exit;

	/** The duration until this state terminates. */
	private int duration;

	/** Ticks remaining until time-out */
	private int remaining;

	public State() {
		duration = remaining = FOREVER;
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

	public void terminate() {
		remaining = 0;
	}

	public boolean isTerminated() {
		return remaining == 0;
	}

	public void setDuration(int frames) {
		if (frames < 0 && frames != FOREVER) {
			throw new IllegalStateException();
		}
		remaining = duration = frames;
	}

	public int getDuration() {
		return duration;
	}

	public int getRemaining() {
		return remaining;
	}
}