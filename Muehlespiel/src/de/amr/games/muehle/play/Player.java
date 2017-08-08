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

	public void stonePlaced();

	public int getStonesPlaced();

	public boolean canJump();

	public OptionalInt supplyPlacePosition();

	public OptionalInt supplyRemovalPosition(StoneColor otherColor);

	public OptionalInt supplyMoveStartPosition();

	public OptionalInt supplyMoveEndPosition(int from);

}