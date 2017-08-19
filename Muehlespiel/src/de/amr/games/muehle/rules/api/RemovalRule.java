package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public interface RemovalRule {

	public OptionalInt supplyPosition(Board board, StoneColor color);

	public String getDescription();
}
