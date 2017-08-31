package de.amr.games.muehle.play;

import java.util.Optional;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;

public interface MillGame {

	static final int NUM_STONES = 9;

	Board getBoard();

	boolean isPlacing();

	int getNumStonesPlaced(int turn);

	boolean isMoving();

	boolean isRemoving();

	boolean isGameOver();

	int getTurn();

	Player getPlayerInTurn();

	Player getPlayerNotInTurn();

	boolean isMoveStartPossible();

	Optional<Move> getMove();

}
