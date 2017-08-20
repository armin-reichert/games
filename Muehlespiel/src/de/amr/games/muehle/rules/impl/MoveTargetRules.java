package de.amr.games.muehle.rules.impl;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.MoveTargetRule;

public enum MoveTargetRules implements MoveTargetRule {

	CLOSE_MILL("An Position %d kann eine Mühle geschlossen werden", (player, from) -> {
		return player.canJump()
				? randomElement(player.getBoard().positions().filter(player.getBoard()::isEmptyPosition)
						.filter(to -> player.getBoard().isMillClosedByJump(from, to, player.getColor())))
				: randomElement(player.getBoard().positions().filter(player.getBoard()::isEmptyPosition)
						.filter(to -> player.getBoard().isMillClosedByMove(from, to, player.getColor())));
	}),

	RANDOM("Position %d ist freie Nachbarposition", (player, from) -> {
		return player.canJump() ? randomElement(player.getBoard().positions().filter(player.getBoard()::isEmptyPosition))
				: randomElement(player.getBoard().emptyNeighbors(from));
	})

	;

	@Override
	public OptionalInt supplyPosition(Player player, int from) {
		return condition.apply(player, from) ? positionSupplier.apply(player, from) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private MoveTargetRules(String description, BiFunction<Player, Integer, OptionalInt> positionSupplier,
			BiFunction<Player, Integer, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private MoveTargetRules(String description, BiFunction<Player, Integer, OptionalInt> positionSupplier) {
		this(description, positionSupplier, (player, from) -> true);
	}

	private final String description;
	private final BiFunction<Player, Integer, OptionalInt> positionSupplier;
	private final BiFunction<Player, Integer, Boolean> condition;
}