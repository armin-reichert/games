package de.amr.games.muehle.rules.impl;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.MovingRule;
import de.amr.games.muehle.rules.api.TriFunction;

public enum MovingRules implements MovingRule {
	CAN_CLOSE_MILL("Durch Zug von Position %d kann eine Mühle geschlossen werden", (board, player) -> {
		StoneColor color = player.getColor();
		return randomElement(board.positions(color).filter(
				p -> player.canJump() ? board.canCloseMillJumpingFrom(p, color) : board.canCloseMillMovingFrom(p, color)));
	}, (board, player, from) -> {
		StoneColor color = player.getColor();
		return randomElement(board.emptyPositions().filter(to -> player.canJump()
				? board.isMillClosedByJump(from, to, color) : board.isMillClosedByMove(from, to, color)));
	}),

	RANDOM("Führe beliebigen möglichen Zug aus", (board, player) -> {
		StoneColor color = player.getColor();
		return randomElement(board.positions(color).filter(p -> player.canJump() || board.hasEmptyNeighbor(p)));
	}, (board, player, from) -> {
		return player.canJump() ? randomElement(board.emptyPositions()) : randomElement(board.emptyNeighbors(from));
	});

	private MovingRules(String description, BiFunction<Board, Player, OptionalInt> startPositionSupplier,
			TriFunction<Board, Player, Integer, OptionalInt> targetPositionSupplier,
			BiFunction<Board, Player, Boolean> condition) {
		this.description = description;
		this.startPositionSupplier = startPositionSupplier;
		this.targetPositionSupplier = targetPositionSupplier;
		this.condition = condition;
	}

	private MovingRules(String description, BiFunction<Board, Player, OptionalInt> startPositionSupplier,
			TriFunction<Board, Player, Integer, OptionalInt> targetPositionSupplier) {
		this(description, startPositionSupplier, targetPositionSupplier, (board, player) -> true);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public OptionalInt supplyMoveStartPosition(Player player) {
		return condition.apply(player.getBoard(), player) ? startPositionSupplier.apply(player.getBoard(), player)
				: OptionalInt.empty();
	}

	@Override
	public OptionalInt supplyMoveTargetPosition(Player player, int from) {
		return targetPositionSupplier.apply(player.getBoard(), player, from);
	}

	private final String description;
	private final BiFunction<Board, Player, OptionalInt> startPositionSupplier;
	private final TriFunction<Board, Player, Integer, OptionalInt> targetPositionSupplier;
	private final BiFunction<Board, Player, Boolean> condition;
}