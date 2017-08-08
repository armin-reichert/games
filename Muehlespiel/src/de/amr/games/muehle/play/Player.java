package de.amr.games.muehle.play;

import java.util.OptionalInt;

import de.amr.games.muehle.board.StoneColor;

/**
 * Common interface for players like interactive or automated player.
 * 
 * @author Armin Reichert
 */
public interface Player {

	public void init();

	public StoneColor getColor();

	public int getStonesPlaced();

	public boolean canJump();

	public OptionalInt tryToPlaceStone();

	public OptionalInt tryToRemoveStone(StoneColor opponentColor);

	public OptionalInt supplyMoveStart();

	public OptionalInt supplyMoveEnd(int from);

}
