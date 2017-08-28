package de.amr.games.muehle.play;

import java.util.Optional;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;

public interface MillGame {

	public Board getBoard();

	public boolean isPlacing();

	public int getNumStonesPlaced(int turn);

	public boolean isMoving();

	public boolean isRemoving();

	public boolean isGameOver();

	public int getTurn();

	public Player getPlayerInTurn();

	public Player getPlayerNotInTurn();

	public boolean isMoveStartPossible();

	public Optional<Move> getMove();

}
