package de.amr.games.pacman.play;

import static de.amr.games.pacman.core.board.BonusSymbol.Apple;
import static de.amr.games.pacman.core.board.BonusSymbol.Bell;
import static de.amr.games.pacman.core.board.BonusSymbol.Cherries;
import static de.amr.games.pacman.core.board.BonusSymbol.Galaxian;
import static de.amr.games.pacman.core.board.BonusSymbol.Grapes;
import static de.amr.games.pacman.core.board.BonusSymbol.Key;
import static de.amr.games.pacman.core.board.BonusSymbol.Peach;
import static de.amr.games.pacman.core.board.BonusSymbol.Strawberry;
import static de.amr.games.pacman.core.board.TileContent.GhostHouse;
import static de.amr.games.pacman.core.board.TileContent.Tunnel;

import java.util.Random;

import de.amr.easy.game.timing.Motor;
import de.amr.games.pacman.core.board.Board;
import de.amr.games.pacman.core.board.BonusSymbol;
import de.amr.games.pacman.core.board.TileContent;
import de.amr.games.pacman.core.entities.PacMan;
import de.amr.games.pacman.core.entities.PacManState;
import de.amr.games.pacman.core.entities.ghost.Ghost;
import de.amr.games.pacman.core.entities.ghost.behaviors.GhostState;

/**
 * Parameters for playing.
 * 
 * @see <a href="http://www.gamasutra.com/view/feature/3938/the_pacman_dossier.php">The Pac-Man
 *      Dossier</a>.
 * 
 * @author Armin Reichert
 */
public class PlaySceneValues {

	private static final Object[][] LEVELS = {
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

	private final Random rand = new Random();
	private final Board board;
	private final Motor motor;
	private final float baseSpeed;

	public PlaySceneValues(Board board, Motor motor, float pixelsPerSecond) {
		this.board = board;
		this.motor = motor;
		this.baseSpeed = pixelsPerSecond / motor.getFrequency();
	}

	public BonusSymbol getBonusSymbol(int level) {
		return (BonusSymbol) LEVELS[level][0];
	}

	public int getBonusValue(int level) {
		return (Integer) LEVELS[level][1];
	}

	public float getPacManSpeed(PacMan pacMan, int level) {
		float speed = baseSpeed * (Float) LEVELS[level][2];
		return pacMan.state() == PacManState.PowerWalking ? getPacManPowerWalkingSpeed(level) : speed;
	}

	public float getGhostSpeedNormal(int level) {
		return baseSpeed * (Float) LEVELS[level][4];
	}

	public float getGhostSpeedInTunnel(int level) {
		return baseSpeed * (Float) LEVELS[level][5];
	}

	public float getGhostSpeedInHouse() {
		return baseSpeed / 2;
	}

	public float getPacManPowerWalkingSpeed(int level) {
		return baseSpeed * (Float) LEVELS[level][10];
	}

	public float getGhostSpeedWhenFrightened(int level) {
		return baseSpeed * (Float) LEVELS[level][12];
	}

	public int getGhostFrightenedDuration(int level) {
		return (Integer) LEVELS[level][13];
	}

	public float getGhostSpeed(Ghost ghost, int level) {
		TileContent content = board.getContent(ghost.currentTile());
		if (content == Tunnel) {
			return getGhostSpeedInTunnel(level);
		} else if (content == GhostHouse) {
			return getGhostSpeedInHouse();
		} else if (ghost.state() == GhostState.Frightened) {
			return getGhostSpeedWhenFrightened(level);
		} else {
			return getGhostSpeedNormal(level);
		}
	}

	public int getGhostWaitingDuration(Ghost ghost) {
		switch (ghost.getName()) {
		case "Blinky":
			return 0;
		case "Pinky":
			return 0;
		case "Inky":
		case "Clyde":
		default:
			return motor.toFrames(2 + rand.nextInt(3));
		}
	}

	public int getGhostRecoveringDuration(Ghost ghost) {
		return motor.toFrames(1 + rand.nextInt(2));
	}
}
