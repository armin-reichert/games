package de.amr.games.pacman.model;

import static de.amr.games.pacman.PacManApp.TS;

import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan;

public class Game {

	/** Tiles per second. */
	private static float tps(float value) {
		return value * TS / 60;
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
		case DEAD:
			return tps(10f);
		case FRIGHTENED:
			return tps(4f);
		case RECOVERING:
			return tps(3f);
		case SCATTERING:
			return tps(6f);
		case STARRED:
			return 0f;
		default:
			throw new IllegalStateException();
		}
	}

	public int level;
	public int lives;
	public int score;
	public int dotsEatenInLevel;
	public int deadGhostScore;

	public Game() {
		level = 1;
		lives = 3;
		score = 0;
		dotsEatenInLevel = 0;
		deadGhostScore = 0;
	}
}