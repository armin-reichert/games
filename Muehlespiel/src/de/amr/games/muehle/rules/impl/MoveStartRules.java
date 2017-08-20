package de.amr.games.muehle.rules.impl;

import static de.amr.games.muehle.util.Util.randomElement;

import java.util.OptionalInt;
import java.util.function.Function;

import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.MoveStartRule;

public enum MoveStartRules implements MoveStartRule {
	CAN_CLOSE_MILL(
			"Von Position %d kann eine Mühle geschlossen werden",
			player -> player.canJump()
					? randomElement(player.getBoard().positions(player.getColor())
							.filter(p -> player.getBoard().canCloseMillJumpingFrom(p, player.getColor())))
					: randomElement(player.getBoard().positions(player.getColor())
							.filter(p -> player.getBoard().canCloseMillMovingFrom(p, player.getColor())))),

	CAN_MOVE(
			"Starte von Position %d, sie besitzt freie Nachbarposition",
			player -> randomElement(player.getBoard().positions(player.getColor())
					.filter(p -> player.canJump() || player.getBoard().hasEmptyNeighbor(p))));

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public OptionalInt supplyPosition(Player player) {
		return condition.apply(player) ? positionSupplier.apply(player) : OptionalInt.empty();
	}

	private MoveStartRules(String description, Function<Player, OptionalInt> positionSupplier,
			Function<Player, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private MoveStartRules(String description, Function<Player, OptionalInt> positionSupplier) {
		this(description, positionSupplier, player -> true);
	}

	private final String description;
	private final Function<Player, OptionalInt> positionSupplier;
	private final Function<Player, Boolean> condition;
}