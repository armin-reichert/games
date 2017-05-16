package de.amr.games.pacman.play;

import static de.amr.games.pacman.core.board.BonusSymbol.Apple;
import static de.amr.games.pacman.core.board.BonusSymbol.Bell;
import static de.amr.games.pacman.core.board.BonusSymbol.Cherries;
import static de.amr.games.pacman.core.board.BonusSymbol.Galaxian;
import static de.amr.games.pacman.core.board.BonusSymbol.Grapes;
import static de.amr.games.pacman.core.board.BonusSymbol.Key;
import static de.amr.games.pacman.core.board.BonusSymbol.Peach;
import static de.amr.games.pacman.core.board.BonusSymbol.Strawberry;

import de.amr.games.pacman.core.board.BonusSymbol;

/**
 * Provides level-specific data.
 * 
 * @see <a href="http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php">The Pac-Man
 *      Dossier</a>.
 * 
 * @author Armin Reichert
 */
public class PacManGameLevels {

	private static final Object[][] LEVEL_DATA = {
		/*@formatter:off*/
		{},
		{ Cherries, 		100, 	.80f, .71f, .75f, .40f, 20, .8f, 10, 	.85f, 	.90f, 	.79f, 	.50f, 6 },
		{ Strawberry, 	300, 	.90f, .79f, .85f, .45f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 5 },
		{ Peach, 				500, 	.90f, .79f, .85f, .45f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 4 },
		{ Peach, 				500, 	.90f, .79f, .85f, .50f, 20, .8f, 10, 	.85f, 	.95f, 	.79f, 	.55f, 3 },
		{ Apple, 				700, 		1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f,   	1f, 	.79f, 	.60f, 2 },
		{ Apple, 				700, 		1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 5 },
		{ Grapes, 			1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Grapes, 			1000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Galaxian, 		2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Galaxian, 		2000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 5 },
		{ Bell, 				3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 2 },
		{ Bell, 				3000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 },
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 3 }, 
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 }, 
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 1 }, 
		{ Key, 					5000, 	1f, .87f, .95f, .50f, 20, .8f, 10, 	.85f, 		1f, 	.79f, 	.60f, 0 }, 
		/*@formatter:on*/
	};

	private float baseSpeed;

	public PacManGameLevels(float baseSpeed) {
		this.baseSpeed = baseSpeed;
	}

	public BonusSymbol getBonusSymbol(int level) {
		return (BonusSymbol) LEVEL_DATA[level][0];
	}

	public int getBonusValue(int level) {
		return (Integer) LEVEL_DATA[level][1];
	}

	public float getPacManSpeed(int level) {
		return baseSpeed * (Float) LEVEL_DATA[level][2];
	}

	public float getGhostSpeedNormal(int level) {
		return baseSpeed * (Float) LEVEL_DATA[level][4];
	}

	public float getGhostSpeedInTunnel(int level) {
		return baseSpeed * (Float) LEVEL_DATA[level][5];
	}

	public float getGhostSpeedInHouse() {
		return baseSpeed / 2;
	}

	public float getPacManAttackingSpeed(int level) {
		return baseSpeed * (Float) LEVEL_DATA[level][10];
	}

	public float getGhostSpeedWhenFrightened(int level) {
		return baseSpeed * (Float) LEVEL_DATA[level][12];
	}

	public int getGhostFrightenedDuration(int level) {
		return (Integer) LEVEL_DATA[level][13];
	}
}
