package de.amr.games.muehle.rules.impl;

import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.RemovalRule;
import de.amr.games.muehle.util.Util;

public enum RemovalRules implements RemovalRule {

	RANDOM(
			"Entferne Stein auf zufällig gewählter Position %d",
			(player, removalColor) -> Util.randomElement(player.getBoard().positions(removalColor)))

	;

	@Override
	public OptionalInt supplyPosition(Player player, StoneColor removalColor) {
		return condition.apply(player, removalColor) ? positionSupplier.apply(player, removalColor) : OptionalInt.empty();
	}

	@Override
	public String getDescription() {
		return description;
	}

	private RemovalRules(String description, BiFunction<Player, StoneColor, OptionalInt> positionSupplier,
			BiFunction<Player, StoneColor, Boolean> condition) {
		this.description = description;
		this.positionSupplier = positionSupplier;
		this.condition = condition;
	}

	private RemovalRules(String description, BiFunction<Player, StoneColor, OptionalInt> positionSupplier) {
		this(description, positionSupplier, (player, removalColor) -> true);
	}

	private final String description;
	private final BiFunction<Player, StoneColor, OptionalInt> positionSupplier;
	private final BiFunction<Player, StoneColor, Boolean> condition;
}