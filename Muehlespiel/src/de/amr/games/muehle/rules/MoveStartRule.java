package de.amr.games.muehle.rules;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;

public enum MoveStartRule implements PositionSelectionRule {
	CAN_CLOSE_MILL_FROM(
			"Von Position %d kann eine Mühle geschlossen werden",
			(board, color) -> randomElement(board.positions(color).filter(p -> board.canCloseMillFrom(p, color)))),

	HAS_EMPTY_NEIGHBOR(
			"Position %d besitzt freie Nachbarposition",
			(board, color) -> randomElement(board.positions(color).filter(board::hasEmptyNeighbor)));

	@Override
	public OptionalInt selectPosition(Board board, StoneColor color) {
		return condition.apply(board, color) ? positionSupplier.apply(board, color) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private MoveStartRule(String description, BiFunction<Board, StoneColor, OptionalInt> placingPositionSupplier,
			BiFunction<Board, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = placingPositionSupplier;
		this.condition = condition;
	}

	private MoveStartRule(String description, BiFunction<Board, StoneColor, OptionalInt> placingPositionSupplier) {
		this(description, placingPositionSupplier, (board, color) -> true);
	}

	private final String description;
	private final BiFunction<Board, StoneColor, OptionalInt> positionSupplier;
	private final BiFunction<Board, StoneColor, Boolean> condition;
}