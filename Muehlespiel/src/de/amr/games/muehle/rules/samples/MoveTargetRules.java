package de.amr.games.muehle.rules.samples;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.MoveTargetRule;
import de.amr.games.muehle.rules.api.TriFunction;

public enum MoveTargetRules implements MoveTargetRule {

	CLOSE_MILL(
			"An Position %d kann eine Mühle geschlossen werden",
			(board, color, from) -> randomElement(
					board.positions().filter(board::isEmptyPosition).filter(to -> board.isMillClosedByMove(from, to, color)))),

	RANDOM("Position %d ist freie Nachbarposition", (board, color, from) -> randomElement(board.emptyNeighbors(from)))

	;

	@Override
	public OptionalInt supplyPosition(Board board, StoneColor color, int from) {
		return condition.apply(board, color, from) ? positionSupplier.apply(board, color, from) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private MoveTargetRules(String description, TriFunction<Board, StoneColor, Integer, OptionalInt> positionSupplier,
			TriFunction<Board, StoneColor, Integer, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private MoveTargetRules(String description, TriFunction<Board, StoneColor, Integer, OptionalInt> positionSupplier) {
		this(description, positionSupplier, (board, color, pos) -> true);
	}

	private final String description;
	private final TriFunction<Board, StoneColor, Integer, OptionalInt> positionSupplier;
	private final TriFunction<Board, StoneColor, Integer, Boolean> condition;
}