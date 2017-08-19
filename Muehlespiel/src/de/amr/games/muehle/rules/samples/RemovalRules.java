package de.amr.games.muehle.rules.samples;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.RemovalRule;
import de.amr.games.muehle.util.Util;

public enum RemovalRules implements RemovalRule {

	RANDOM(
			"Entferne Stein auf zufällig gewählter Position %d",
			(board, color) -> Util.randomElement(board.positions(color)))

	;

	@Override
	public OptionalInt supplyPosition(Board board, StoneColor color) {
		return condition.apply(board, color) ? positionSupplier.apply(board, color) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private RemovalRules(String description, BiFunction<Board, StoneColor, OptionalInt> placingPositionSupplier,
			BiFunction<Board, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = placingPositionSupplier;
		this.condition = condition;
	}

	private RemovalRules(String description, BiFunction<Board, StoneColor, OptionalInt> placingPositionSupplier) {
		this(description, placingPositionSupplier, (board, color) -> true);
	}

	private final String description;
	private final BiFunction<Board, StoneColor, OptionalInt> positionSupplier;
	private final BiFunction<Board, StoneColor, Boolean> condition;
}