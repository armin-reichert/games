package de.amr.games.muehle.rules;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.util.Util;

public enum MoveTargetRule implements PositionSelectionRule {

	CLOSE_MILL(
			"Von Position %d kann eine Mühle geschlossen werden",
			(board, color, from) -> Util.randomElement(
					board.positions().filter(board::isEmptyPosition).filter(to -> board.isMillClosedByMove(from, to, color)))),

	RANDOM_NEIGHBOR(
			"Position %d ist freie Nachbarposition",
			(board, color, from) -> randomElement(board.emptyNeighbors(from)))

	;

	@Override
	public OptionalInt selectPosition(Board board, StoneColor color, int from) {
		return condition.apply(board, color) ? positionSupplier.apply(board, color, from) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private MoveTargetRule(String description,
			TriFunction<Board, StoneColor, Integer, OptionalInt> placingPositionSupplier,
			BiFunction<Board, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = placingPositionSupplier;
		this.condition = condition;
	}

	private MoveTargetRule(String description,
			TriFunction<Board, StoneColor, Integer, OptionalInt> placingPositionSupplier) {
		this(description, placingPositionSupplier, (board, color) -> true);
	}

	private final String description;
	private final TriFunction<Board, StoneColor, Integer, OptionalInt> positionSupplier;
	private final BiFunction<Board, StoneColor, Boolean> condition;

}