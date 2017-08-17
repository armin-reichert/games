package de.amr.games.muehle.rules;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public interface PositionSupplyRule {

	public String getDescription();

	public OptionalInt supplyPosition(Board board, StoneColor color);
}
