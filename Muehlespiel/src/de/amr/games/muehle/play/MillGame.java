package de.amr.games.muehle.play;

import java.util.Optional;

import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;

public interface MillGame {

	public boolean isPlacing();

	public boolean isMoving();

	public boolean isRemoving();

	public boolean isGameOver();

	public Player getPlayerInTurn();

	public Player getPlayerNotInTurn();

	public boolean isMoveStartPossible();

	public Optional<Move> getMove();

}
