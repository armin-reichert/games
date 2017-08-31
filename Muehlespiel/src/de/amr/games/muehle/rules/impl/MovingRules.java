package de.amr.games.muehle.rules.impl;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.Optional;
import java.util.OptionalInt;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.MovingRule;
import de.amr.games.muehle.rules.api.TriFunction;

public enum MovingRules implements MovingRule {

	CAN_CLOSE_MILL("Durch Zug %d -> %d kann eine Mühle geschlossen werden", (board, player, color) -> {
		OptionalInt from = randomElement(board.positions(color).filter(
				p -> player.canJump() ? board.canCloseMillJumpingFrom(p, color) : board.canCloseMillMovingFrom(p, color)));
		if (from.isPresent()) {
			OptionalInt to = randomElement(board.emptyPositions().filter(p -> player.canJump()
					? board.isMillClosedByJump(from.getAsInt(), p, color) : board.isMillClosedByMove(from.getAsInt(), p, color)));
			if (to.isPresent()) {
				return Optional.of(new Move(from.getAsInt(), to.getAsInt()));
			}
		}
		return Optional.empty();
	}),

	RANDOM("Führe beliebigen Zug %d -> %d aus", (board, player, color) -> {
		OptionalInt from = randomElement(board.positions(color).filter(p -> player.canJump() || board.hasEmptyNeighbor(p)));
		if (from.isPresent()) {
			OptionalInt to = player.canJump() ? randomElement(board.emptyPositions())
					: randomElement(board.emptyNeighbors(from.getAsInt()));
			if (to.isPresent()) {
				return Optional.of(new Move(from.getAsInt(), to.getAsInt()));
			}
		}
		return Optional.empty();
	});

	private MovingRules(String description, TriFunction<Board, Player, StoneColor, Optional<Move>> moveSupplier,
			TriFunction<Board, Player, StoneColor, Boolean> condition) {
		this.description = description;
		this.moveSupplier = moveSupplier;
		this.condition = condition;
	}

	private MovingRules(String description, TriFunction<Board, Player, StoneColor, Optional<Move>> moveSupplier) {
		this(description, moveSupplier, (board, player, color) -> true);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Optional<Move> supplyMove(Player player) {
		return condition.apply(player.getBoard(), player, player.getColor())
				? moveSupplier.apply(player.getBoard(), player, player.getColor()) : Optional.empty();
	}

	private final String description;
	private final TriFunction<Board, Player, StoneColor, Optional<Move>> moveSupplier;
	private final TriFunction<Board, Player, StoneColor, Boolean> condition;
}