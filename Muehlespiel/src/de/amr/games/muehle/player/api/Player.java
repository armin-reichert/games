package de.amr.games.muehle.player.api;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;

/**
 * Common interface for players like interactive or automated player.
 * 
 * @author Armin Reichert
 */
public interface Player {

	public default String getName() {
		return String.format("%s(%s)", getClass().getSimpleName(), getColor() == StoneColor.WHITE ? "Weiß" : "Schwarz");
	}

	public Board getBoard();

	public StoneColor getColor();

	public OptionalInt supplyPlacingPosition();

	public OptionalInt supplyRemovalPosition();

	public Move supplyMove();

	public void newMove();

	public boolean canJump();
}