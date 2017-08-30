package de.amr.games.muehle.rules.impl;

import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.RemovalRule;
import de.amr.games.muehle.rules.api.TriFunction;
import de.amr.games.muehle.util.Util;

public enum RemovalRules implements RemovalRule {

	STONE_WHICH_CAN_MOVE(
			"Entferne Stein an Position %d, weil er bewegt werden kann",
			(board, player, color) -> Util.randomElement(board.positions(color).filter(board::hasEmptyNeighbor))),

	RANDOM(
			"Entferne Stein auf zufällig gewählter Position %d",
			(board, player, color) -> Util.randomElement(board.positions(color)))

	;

	@Override
	public OptionalInt supplyRemovalPosition(Player player, StoneColor removalColor) {
		return condition.apply(player.getBoard(), player, removalColor)
				? positionSupplier.apply(player.getBoard(), player, removalColor) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private RemovalRules(String description, TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier,
			TriFunction<Board, Player, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private RemovalRules(String description, TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier) {
		this(description, positionSupplier, (board, player, removalColor) -> true);
	}

	private final String description;
	private final TriFunction<Board, Player, StoneColor, OptionalInt> positionSupplier;
	private final TriFunction<Board, Player, StoneColor, Boolean> condition;
}