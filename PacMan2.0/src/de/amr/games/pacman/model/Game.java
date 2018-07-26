package de.amr.games.pacman.model;

import static de.amr.games.pacman.PacManApp.TS;

import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan;

public class Game {

	public static final int PELLET_VALUE = 10;
	public static final int ENERGIZER_VALUE = 50;
	public static final int DOTS_BONUS_1 = 70;
	public static final int DOTS_BONUS_2 = 170;

	public static final int[] GHOST_POINTS = new int[] { 200, 400, 800, 1600 };

	private static float tilesPerSec(float value) {
		return value * TS / 60;
	}

	public float getPacManSpeed(MazeMover<PacMan.State> pacMan) {
		switch (pacMan.getState()) {
		case ALIVE:
			return tilesPerSec(8f);
		case DYING:
			return 0;
		default:
			throw new IllegalStateException();
		}
	}

	public float getGhostSpeed(MazeMover<Ghost.State> ghost) {
		switch (ghost.getState()) {
		case ATTACKING:
			return tilesPerSec(6f);
		case DYING:
			return 0;
		case DEAD:
			return tilesPerSec(12f);
		case FRIGHTENED:
			return tilesPerSec(4f);
		case RECOVERING:
			return tilesPerSec(3f);
		case SCATTERING:
			return tilesPerSec(6f);
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
		maze.init();
		level = 1;
		livesLeft = 3;
		score = 0;
		dotsEaten = 0;
		ghostIndex = 0;
		dotsTotal = maze.tiles().map(maze::getContent).filter(Tile::isFood).count();
	}
}