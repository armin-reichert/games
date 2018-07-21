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
		return tps(8f);
	}

	public float getGhostSpeed(MazeMover<Ghost.State> ghost) {
		float speed = 0;
		switch (ghost.getState()) {
		case ATTACKING:
			speed = tps(6f);
			break;
		case DEAD:
			speed = tps(10f);
			break;
		case FRIGHTENED:
			speed = tps(4f);
			break;
		case RECOVERING:
			speed = tps(3f);
			break;
		case SCATTERING:
			speed = tps(6f);
			break;
		case STARRED:
			speed = 0f;
			break;
		default:
			throw new IllegalStateException();
		}
		return speed;
	}

	public int level;
	public int lives;
	public int score;
	public int dotsEatenInLevel;

	public Game() {
		level = 1;
		lives = 3;
		score = 0;
		dotsEatenInLevel = 0;
	}
}