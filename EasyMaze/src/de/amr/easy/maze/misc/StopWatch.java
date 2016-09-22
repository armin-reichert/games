package de.amr.easy.maze.misc;

public class StopWatch {

	private long start;
	private float duration;

	public void start(String... msg) {
		if (msg.length > 0) {
			System.out.println(msg[0]);
		}
		start = System.currentTimeMillis();
	}

	public void stop(String... msg) {
		duration = (System.currentTimeMillis() - start) / 1000f;
		if (msg.length > 0) {
			System.out.println(String.format(msg[0], duration));
		}
	}

	public float getDuration() {
		return duration;
	}
}
