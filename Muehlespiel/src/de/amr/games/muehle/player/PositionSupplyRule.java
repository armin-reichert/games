package de.amr.games.muehle.player;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public class PositionSupplyRule {

	private final String reason;
	private final BiFunction<Board, StoneColor, OptionalInt> positionSupplier;
	private final BiFunction<Board, StoneColor, Boolean> condition;

	public PositionSupplyRule(String reason, BiFunction<Board, StoneColor, OptionalInt> placingPositionSupplier,
			BiFunction<Board, StoneColor, Boolean> condition) {
		this.reason = reason;
		this.positionSupplier = placingPositionSupplier;
		this.condition = condition;
	}

	public PositionSupplyRule(String reason, BiFunction<Board, StoneColor, OptionalInt> placingPositionSupplier) {
		this(reason, placingPositionSupplier, (board, color) -> true);
	}

	public String getReason() {
		return reason;
	}

	public OptionalInt supplyPosition(Board board, StoneColor color) {
		return condition.apply(board, color) ? positionSupplier.apply(board, color) : OptionalInt.empty();
	}
}
