package de.amr.games.pacman.model;

import static de.amr.games.pacman.PacManApp.TS;

import java.util.function.IntSupplier;

import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan;

public class Game {

	public static final int PELLET_VALUE = 10;
	public static final int ENERGIZER_VALUE = 50;
	public static final int DOTS_BONUS_1 = 70;
	public static final int DOTS_BONUS_2 = 170;

	public static final int[] GHOST_POINTS = new int[] { 200, 400, 800, 1600 };

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
		case ATTACKING:
			return tps(6f);
		case DYING:
			return 0;
		case DEAD:
			return tps(12f);
		case FRIGHTENED:
			return tps(4f);
		case RECOVERING:
			return tps(3f);
		case SCATTERING:
			return tps(6f);
		default:
			throw new IllegalStateException();
		}
	}

	public int level;
	public int livesLeft;
	public int score;
	public long dotsTotal;
	public int dotsEaten;
	public int ghostIndex;

	public void init(Maze maze) {
		maze.resetFood();
		level = 1;
		livesLeft = 3;
		score = 0;
		dotsEaten = 0;
		ghostIndex = 0;
		dotsTotal = maze.tiles().map(maze::getContent).filter(Tile::isFood).count();
	}
}