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

	default String getName() {
		return format("%s(%s)", getClass().getSimpleName(), getColor());
	}

	boolean isInteractive();

	Board getBoard();

	StoneColor getColor();

	default boolean canJump() {
		return getBoard().stoneCount(getColor()) == 3;
	}

	default boolean isTrapped() {
		return getBoard().isTrapped(getColor());
	}

	OptionalInt supplyPlacingPosition();

	OptionalInt supplyRemovalPosition();

	void newMove();

	Optional<Move> supplyMove();
}