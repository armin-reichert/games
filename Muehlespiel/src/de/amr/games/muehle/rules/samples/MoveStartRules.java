package de.amr.games.muehle.rules.samples;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.MoveStartRule;

public enum MoveStartRules implements MoveStartRule {
	CAN_CLOSE_MILL_FROM(
			"Von Position %d kann eine Mühle geschlossen werden",
			(board, color) -> randomElement(board.positions(color).filter(p -> board.canCloseMillFrom(p, color)))),

	HAS_EMPTY_NEIGHBOR(
			"Position %d besitzt freie Nachbarposition",
			(board, color) -> randomElement(board.positions(color).filter(board::hasEmptyNeighbor)));

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public BiFunction<Board, StoneColor, OptionalInt> getPositionSupplier() {
		return positionSupplier;
	}

	@Override
	public BiFunction<Board, StoneColor, Boolean> getCondition() {
		return condition;
	}

	private MoveStartRules(String description, BiFunction<Board, StoneColor, OptionalInt> positionSupplier,
			BiFunction<Board, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private MoveStartRules(String description, BiFunction<Board, StoneColor, OptionalInt> positionSupplier) {
		this(description, positionSupplier, (board, color) -> true);
	}

	private final String description;
	private final BiFunction<Board, StoneColor, OptionalInt> positionSupplier;
	private final BiFunction<Board, StoneColor, Boolean> condition;
}