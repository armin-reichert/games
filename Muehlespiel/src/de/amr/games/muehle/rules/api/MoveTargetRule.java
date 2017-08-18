package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public interface MoveTargetRule {

	public default OptionalInt selectPosition(Board board, StoneColor color, int from) {
		return getCondition().apply(board, color, from) ? getPositionSupplier().apply(board, color, from)
				: OptionalInt.empty();
	}

	public String getDescription();

	public TriFunction<Board, StoneColor, Integer, OptionalInt> getPositionSupplier();

	public TriFunction<Board, StoneColor, Integer, Boolean> getCondition();

}
