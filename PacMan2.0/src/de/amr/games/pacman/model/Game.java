package de.amr.games.pacman.model;

import static de.amr.games.pacman.PacManApp.TS;

import java.util.function.IntSupplier;

import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.games.pacman.ui.actor.MazeMover;
import de.amr.games.pacman.ui.actor.PacMan;

public class Game {

	public static final int PELLET_VALUE = 10;
	public static final int ENERGIZER_VALUE = 50;
	public static final int DOTS_BONUS_1 = 70;
	public static final int DOTS_BONUS_2 = 170;

	public static final int[] GHOST_POINTS = new int[] { 200, 400, 800, 1600 };
	public static final int[] BONUS_POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

	public static IntSupplier fnPulse = () -> 60;

	/** Tiles per second. */
	private static float tps(float value) {
		return (value * TS) / fnPulse.getAsInt();
	}

	public float getPacManSpeed(MazeMover<PacMan.State> pacMan) {
		switch (pacMan.getState()) {
		case ALIVE:
			return tps(8f);
		case DYING:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public float getGhostSpeed(MazeMover<Ghost.State> ghost) {
		switch (ghost.getState()) {
		case AGGRO:
			return tps(6f);
		case DYING:
			return 0;
		case DEAD:
			return tps(12f);
		case AFRAID:
		case BRAVE:
			return tps(4f);
		case SAFE:
			return tps(3f);
		case SCATTERING:
			return tps(6f);
		default:
			throw new IllegalStateException();
		}
	}

	public int level;
	public int lives;
	public int score;
	public long foodTotal;
	public int foodEaten;
	public int ghostIndex;

	public void init(Maze maze) {
		maze.resetFood();
		foodTotal = maze.getFoodCount();
		level = 1;
		lives = 3;
		score = 0;
		foodEaten = 0;
		ghostIndex = 0;
	}
}