package de.amr.easy.game.timing;

public class Countdown {

	private final int duration;
	private int remaining;
	private boolean running;
	private boolean complete;

	public Countdown(int duration) {
		if (duration <= 0) {
			throw new IllegalArgumentException("Countdown duration must be positive");
		}
		this.duration = duration;
		reset();
	}

	public void reset() {
		remaining = duration;
		running = false;
		complete = false;
	}

	public int getDuration() {
		return duration;
	}

	public int getElapsedInPercent() {
		return 100 * (duration - remaining) / duration;
	}

	public int getRemaining() {
		return remaining;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isComplete() {
		return complete;
	}

	public void start() {
		running = true;
	}

	public void restart() {
		reset();
		start();
	}

	public void end() {
		remaining = 0;
		running = false;
		complete = true;
	}

	public void update() {
		if (!running) {
			return;
		}
		if (remaining > 0) {
			--remaining;
		}
		if (remaining == 0) {
			running = false;
			complete = true;
		}
	}
}
