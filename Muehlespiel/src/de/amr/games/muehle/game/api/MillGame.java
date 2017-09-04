package de.amr.games.muehle.game.api;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.player.api.Player;

public interface MillGame {

	static final int NUM_STONES = 9;

	Board getBoard();

	int numWhiteStonesPlaced();

	int numBlackStonesPlaced();

	boolean isPlacing();

	boolean isMoving();

	boolean isRemoving();

	boolean isGameOver();

	Player getWhitePlayer();

	Player getBlackPlayer();

	Player getPlayerInTurn();

	Player getPlayerNotInTurn();
}