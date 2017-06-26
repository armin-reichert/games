package de.amr.easy.game.timing;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.beans.PropertyChangeListener;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Animation motor.
 * 
 * @author Armin Reichert
 */
public class Motor {

	private final Task renderTask;
	private final Task updateTask;
	private Optional<Logger> logger;
	private int frequency;
	private long updateCount;
	private long period;
	private Thread thread;
	private volatile boolean running;

	public Motor(Runnable updateTask, Runnable renderTask) {
		this.updateTask = new Task(updateTask, "ups", SECONDS.toNanos(1));
		this.renderTask = new Task(renderTask, "fps", SECONDS.toNanos(1));
		setFrequency(60);
		setLogger(null);
	}

	public void setLogger(Logger log) {
		this.logger = log != null ? Optional.of(log) : Optional.empty();
	}

	public void setFrequency(int fps) {
		this.frequency = fps;
		period = fps > 0 ? SECONDS.toNanos(1) / fps : Integer.MAX_VALUE;
	}

	public int getFrequency() {
		return frequency;
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public int secToTicks(float seconds) {
		return Math.round(getFrequency() * seconds);
	}

	public float ticksToSec(int ticks) {
		return (float) ticks / getFrequency();
	}

	public synchronized void addRenderListener(PropertyChangeListener observer) {
		renderTask.addListener(observer);
	}

	public synchronized void addUpdateListener(PropertyChangeListener observer) {
		updateTask.addListener(observer);
	}

	public synchronized void start() {
		if (!running) {
			running = true;
			thread = new Thread(this::run, "GameLoop");
			thread.start();
		}
	}

	public synchronized void stop() {
		if (running) {
			running = false;
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void run() {
		long overTime = 0;
		while (running) {
			updateTask.run();
			renderTask.run();
			logger.ifPresent(log -> {
				log.info(format("\nUpdate time:    %10.2f millis", updateTask.getUsedTime() / 1000000f));
				log.info(format("Rendering time: %10.2f millis", renderTask.getUsedTime() / 1000000f));
			});
			++updateCount;
			long usedTime = updateTask.getUsedTime() + renderTask.getUsedTime();
			long timeLeft = (period - usedTime);
			if (timeLeft > 0) {
				long sleepTime = timeLeft;
				try {
					NANOSECONDS.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				logger.ifPresent(log -> {
					log.info(format("Sleep time:     %10.2f millis", sleepTime / 1000000f));
				});
			} else if (timeLeft < 0) {
				overTime += (-timeLeft);
				for (int extraUpdates = 3; extraUpdates > 0 && overTime > period; overTime -= period, --extraUpdates) {
					updateTask.run();
					logger.ifPresent(log -> {
						log.info(format("Extra Update time: %10.2f millis", updateTask.getUsedTime() / 1000000f));
					});
					++updateCount;
				}
			}
		}
	}
}
