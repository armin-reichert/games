package de.amr.games.muehle.player.api;

import static java.lang.String.format;

import java.util.Optional;
import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

/**
 * Common interface for players.
 * 
 * @author Armin Reichert
 */
public interface Player {

	public default String getName() {
		return format("%s(%s)", getClass().getSimpleName(), getColor());
	}

	public Board getBoard();

	public StoneColor getColor();

	public default boolean canJump() {
		return getBoard().stoneCount(getColor()) == 3;
	}

	public OptionalInt supplyPlacingPosition();

	public OptionalInt supplyRemovalPosition();

	public void newMove();

	public Optional<Move> supplyMove();
}