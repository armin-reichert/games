package de.amr.games.pacman.model;

import static de.amr.games.pacman.PacManApp.TS;

import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan;

public class Game {

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
		case DEAD:
			return tilesPerSec(10f);
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