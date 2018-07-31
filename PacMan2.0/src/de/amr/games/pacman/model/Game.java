package de.amr.games.pacman.model;

import static de.amr.games.pacman.model.BonusSymbol.APPLE;
import static de.amr.games.pacman.model.BonusSymbol.BELL;
import static de.amr.games.pacman.model.BonusSymbol.CHERRIES;
import static de.amr.games.pacman.model.BonusSymbol.GALAXIAN;
import static de.amr.games.pacman.model.BonusSymbol.GRAPES;
import static de.amr.games.pacman.model.BonusSymbol.KEY;
import static de.amr.games.pacman.model.BonusSymbol.PEACH;
import static de.amr.games.pacman.model.BonusSymbol.STRAWBERRY;

import java.util.function.IntSupplier;

import de.amr.games.pacman.ui.MazeUI;
import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.games.pacman.ui.actor.MazeMover;
import de.amr.games.pacman.ui.actor.PacMan;

public class Game {

	public final int PELLET_VALUE = 10;
	public final int ENERGIZER_VALUE = 50;
	public final int DOTS_BONUS_1 = 70;
	public final int DOTS_BONUS_2 = 170;
	public final int[] GHOST_POINTS = new int[] { 200, 400, 800, 1600 };

	public final IntSupplier fnPulse;

	private final Object[][] LEVELS = {
		/*@formatter:off*/
		{},
		{ CHERRIES, 		100, 	.80f, .71f, .75f, .40f, 20, .8f, 10, 	.85f, 	.90f, 	.79f, 	.50f, 6 },
		{ STRAWBERRY, 	300, 	.90f, .79f, .85f, .45f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 5 },
		{ PEACH, 				500, 	.90f, .79f, .85f, .45f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 4 },
		{ PEACH, 				500, 	.90f, .79f, .85f, .50f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 3 },
		{ APPLE, 				700, 		1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f,   	1f, 	.79f, 	.60f, 2 },
		{ APPLE, 				700, 		1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 5 },
		{ GRAPES, 			1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ GRAPES, 			1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ GALAXIAN, 		2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ GALAXIAN, 		2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 5 },
		{ BELL, 				3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ BELL, 				3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ KEY, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ KEY, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 3 }, 
		{ KEY, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 }, 
		{ KEY, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 }, 
		{ KEY, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 0 }, 
		/*@formatter:on*/
	};

	/** Tiles per second. */
	private float tps(float value) {
		return (value * MazeUI.TS) / fnPulse.getAsInt();
	}

	public BonusSymbol getBonusSymbol() {
		return (BonusSymbol) LEVELS[level][0];
	}

	public int getBonusValue() {
		return (int) LEVELS[level][1];
	}
	
	public int getGhostValue() {
		return GHOST_POINTS[ghostIndex];
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
	
	public Game(IntSupplier fnPulse) {
		this.fnPulse = fnPulse;
	}

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