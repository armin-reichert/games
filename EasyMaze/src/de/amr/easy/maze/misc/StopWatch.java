package de.amr.easy.maze.misc;

public class StopWatch {

	private long start;
	private float duration;

	public void start(String msg) {
		System.err.println(msg);
		start = System.currentTimeMillis();
	}

	public void stop() {
		duration = (System.currentTimeMillis() - start) / 1000f;
	}

	public String getDurationSeconds() {
		return String.format("%.6f", duration);
	}
}
