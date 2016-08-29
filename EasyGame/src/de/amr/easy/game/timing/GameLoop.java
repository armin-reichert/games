package de.amr.easy.game.timing;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;

public class GameLoop {

	public boolean log = false;

	private static void sleep(long time, TimeUnit unit) {
		try {
			unit.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private final Task renderTask;
	private final Task updateTask;
	private int fps;
	private long updateCount;
	private long period;
	private Thread thread;
	private volatile boolean running;

	public GameLoop(Runnable updateTask, Runnable renderTask) {
		this.updateTask = new Task(updateTask, "ups", SECONDS.toNanos(1));
		this.renderTask = new Task(renderTask, "fps", SECONDS.toNanos(1));
		setFrameRate(60);
	}

	public void setFrameRate(int fps) {
		this.fps = fps;
		period = fps > 0 ? SECONDS.toNanos(1) / fps : Integer.MAX_VALUE;
	}

	public int getFrameRate() {
		return fps;
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public int secToFrames(float seconds) {
		return Math.round(getFrameRate() * seconds);
	}

	public int framesToSec(int frames) {
		return frames / getFrameRate();
	}

	public synchronized void addFPSListener(PropertyChangeListener observer) {
		renderTask.addListener(observer);
	}

	public synchronized void addUPSListener(PropertyChangeListener observer) {
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

	private void printDuration(Task task, String taskName) {
		out.println(String.format("%s time: %f millis", taskName, task.getUsedTime() / 1000000f));
	}

	private void run() {
		long overTime = 0;
		while (running) {
			updateTask.run();
			if (log) {
				printDuration(updateTask, "\nUpdate");
			}
			++updateCount;
			renderTask.run();
			if (log) {
				printDuration(renderTask, "Rendering");
			}
			long timeLeft = period - (updateTask.getUsedTime() + renderTask.getUsedTime());
			if (timeLeft > 0) {
				long sleepTime = timeLeft;
				sleep(sleepTime, NANOSECONDS);
				if (log) {
					out.println(String.format("Sleep time: %f millis", sleepTime / 1000000f));
				}
			} else if (timeLeft < 0) {
				overTime += (-timeLeft);
				for (int extraUpdates = 2; extraUpdates > 0
						&& overTime > period; overTime -= period, --extraUpdates) {
					updateTask.run();
					if (log) {
						printDuration(updateTask, "- Extra Update");
					}
					++updateCount;
				}
			}
		}
	}
}
