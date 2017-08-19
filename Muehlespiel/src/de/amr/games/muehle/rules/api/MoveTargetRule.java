package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public interface MoveTargetRule {

	public OptionalInt supplyPosition(Board board, StoneColor color, int from);

	public String getDescription();
}