package de.amr.games.pacman.fsm;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import de.amr.easy.game.Application;

/**
 * A state of a finite state machine.
 */
public class State {
	
	public static final int FOREVER = -1;
	
	/** The action performed when entering this state. */
	public Consumer<State> entry;

	/** The action performed when in this state. */
	public Consumer<State> update;

	/** The action performed when exiting this state. */
	public Consumer<State> exit;

	/** The condition denoting when this state is terminated. */
	public BooleanSupplier isTerminated;

	/** The duration until this state terminates. */
	private int duration;

	private int timer;

	public State() {
		isTerminated = () -> false;
		duration = FOREVER;
		timer = FOREVER;
	}

	void doEntry() {
		if (entry != null) {
			entry.accept(this);
		}
		resetTimer();
	}

	void doExit() {
		if (exit != null) {
			exit.accept(this);
		}
	}

	void doUpdate() {
		if (update != null) {
			update.accept(this);
		}
		if (timer > 0) {
			--timer;
		}
	}
	
	public void terminate() {
		timer = 0;
	}

	public boolean isTerminated() {
		return isTerminated.getAsBoolean() || timer == 0;
	}

	public void setDuration(int frames) {
		if (frames < 0 && frames != FOREVER) {
			throw new IllegalStateException();
		}
		timer = duration = frames;
	}

	public void resetTimer() {
		timer = duration;
	}
	
	public int getDuration() {
		return duration;
	}

	public int getTimer() {
		return timer;
	}
}