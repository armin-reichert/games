package de.amr.games.muehle.rules.impl;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.MovingRule;

public enum MovingRules implements MovingRule {
	CAN_CLOSE_MILL("Durch Zug von Position %d kann eine Mühle geschlossen werden", player -> {
		Board board = player.getBoard();
		StoneColor color = player.getColor();
		/*@formatter:off*/
		return randomElement(board.positions(color)
				.filter(p -> player.canJump() 
						? board.canCloseMillJumpingFrom(p, color)
						: board.canCloseMillMovingFrom(p, color)));
		/*@formatter:on*/
	}, (player, from) -> {
		Board board = player.getBoard();
		StoneColor color = player.getColor();
		return randomElement(board.emptyPositions().filter(to -> player.canJump()
				? board.isMillClosedByJump(from, to, color) : board.isMillClosedByMove(from, to, color)));
	}),

	RANDOM("Führe beliebigen möglichen Zug aus", player -> {
		Board board = player.getBoard();
		StoneColor color = player.getColor();
		return randomElement(
				board.positions(color).filter(p -> player.canJump() || board.hasEmptyNeighbor(p)));
	}, (player, from) -> {
		Board board = player.getBoard();
		return player.canJump() ? randomElement(board.emptyPositions())
				: randomElement(board.emptyNeighbors(from));
	});

	private MovingRules(String description, Function<Player, OptionalInt> startPositionSupplier,
			BiFunction<Player, Integer, OptionalInt> targetPositionSupplier,
			Function<Player, Boolean> condition) {
		this.description = description;
		this.startPositionSupplier = startPositionSupplier;
		this.targetPositionSupplier = targetPositionSupplier;
		this.condition = condition;
	}

	private MovingRules(String description, Function<Player, OptionalInt> startPositionSupplier,
			BiFunction<Player, Integer, OptionalInt> targetPositionSupplier) {
		this(description, startPositionSupplier, targetPositionSupplier, player -> true);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public OptionalInt supplyMoveStartPosition(Player player) {
		return condition.apply(player) ? startPositionSupplier.apply(player) : OptionalInt.empty();
	}

	@Override
	public OptionalInt supplyMoveTargetPosition(Player player, int from) {
		return targetPositionSupplier.apply(player, from);
	}

	private final String description;
	private final Function<Player, OptionalInt> startPositionSupplier;
	private final BiFunction<Player, Integer, OptionalInt> targetPositionSupplier;
	private final Function<Player, Boolean> condition;
}