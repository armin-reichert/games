package de.amr.games.muehle.player;

import java.util.OptionalInt;

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

	public StoneColor getColor();

	public OptionalInt supplyPlacePosition();

	public OptionalInt supplyRemovalPosition(StoneColor otherColor);

	public Move supplyMove(boolean canJump);

	public default void clearMove() {
	}
}