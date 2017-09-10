package de.amr.games.muehle.player;

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

	default String name() {
		return format("%s(%s)", getClass().getSimpleName(), color());
	}

	boolean isInteractive();

	Board board();

	StoneColor color();

	default boolean canJump() {
		return board().stoneCount(color()) == 3;
	}

	default boolean isTrapped() {
		return board().isTrapped(color());
	}

	OptionalInt supplyPlacingPosition();

	OptionalInt supplyRemovalPosition();

	void newMove();

	Optional<Move> supplyMove();
}