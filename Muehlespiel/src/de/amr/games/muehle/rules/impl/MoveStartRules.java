package de.amr.games.muehle.rules.impl;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.MoveStartRule;

public enum MoveStartRules implements MoveStartRule {
	CAN_CLOSE_MILL(
			"Von Position %d kann eine Mühle geschlossen werden",
			(board, color) -> board.canJump(color)
					? randomElement(board.positions(color).filter(p -> board.canCloseMillJumpingFrom(p, color)))
					: randomElement(board.positions(color).filter(p -> board.canCloseMillMovingFrom(p, color)))),

	CAN_MOVE(
			"Starte von Position %d, sie besitzt freie Nachbarposition",
			(board, color) -> randomElement(
					board.positions(color).filter(p -> board.canJump(color) || board.hasEmptyNeighbor(p))));

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public OptionalInt supplyPosition(Board board, StoneColor color) {
		return condition.apply(board, color) ? positionSupplier.apply(board, color) : OptionalInt.empty();
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