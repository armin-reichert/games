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

	public void init();

	public StoneColor getColor();

	public void stonePlaced();

	public int getStonesPlaced();

	public boolean canJump();

	public OptionalInt supplyPlacePosition();

	public OptionalInt supplyRemovalPosition(StoneColor otherColor);

	public Move supplyMove();

	public void clearMove();
}