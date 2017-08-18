package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public interface TargetPositionSelector {

	public default OptionalInt selectPosition(Board board, StoneColor color, int relatedPosition) {
		return OptionalInt.of(relatedPosition);
	}

}
