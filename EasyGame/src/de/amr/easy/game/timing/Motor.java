package de.amr.easy.game.timing;

import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.beans.PropertyChangeListener;

public class Motor {

	public boolean log = false;

	private final Task renderTask;
	private final Task updateTask;
	private int targetFPS;
	private long updateCount;
	private long period;
	private Thread thread;
	private volatile boolean running;

	public Motor(Runnable updateTask, Runnable renderTask) {
		this.updateTask = new Task(updateTask, "ups", SECONDS.toNanos(1));
		this.renderTask = new Task(renderTask, "fps", SECONDS.toNanos(1));
		setTargetFrameRate(60);
	}

	public void setTargetFrameRate(int fps) {
		this.targetFPS = fps;
		period = fps > 0 ? SECONDS.toNanos(1) / fps : Integer.MAX_VALUE;
	}

	public int getTargetFrameRate() {
		return targetFPS;
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public int secToFrames(float seconds) {
		return Math.round(getTargetFrameRate() * seconds);
	}

	public int framesToSec(int frames) {
		return frames / getTargetFrameRate();
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

	private void run() {
		long overTime = 0;
		while (running) {
			updateTask.run();
			renderTask.run();
			if (log) {
				out.println();
				out.println(format("Update time:    %10.2f millis", updateTask.getUsedTime() / 1000000f));
				out.println(format("Rendering time: %10.2f millis", renderTask.getUsedTime() / 1000000f));
			}
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
				if (log) {
					out.println(format("Sleep time:     %10.2f millis", sleepTime / 1000000f));
				}
			} else if (timeLeft < 0) {
				overTime += (-timeLeft);
				for (int extraUpdates = 3; extraUpdates > 0 && overTime > period; overTime -= period, --extraUpdates) {
					updateTask.run();
					if (log) {
						out.println(format("Extra Update time: %10.2f millis", updateTask.getUsedTime() / 1000000f));
					}
					++updateCount;
				}
			}
		}
	}
}
