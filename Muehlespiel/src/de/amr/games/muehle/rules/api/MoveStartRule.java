package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public interface MoveStartRule {

	public default OptionalInt selectPosition(Board board, StoneColor color) {
		return getCondition().apply(board, color) ? getPositionSupplier().apply(board, color) : OptionalInt.empty();
	}

	public String getDescription();

	public BiFunction<Board, StoneColor, OptionalInt> getPositionSupplier();

	public BiFunction<Board, StoneColor, Boolean> getCondition();
}