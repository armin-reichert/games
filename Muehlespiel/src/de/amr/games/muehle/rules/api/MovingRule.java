package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.player.api.Player;

public interface MovingRule extends Rule {

	public OptionalInt supplyMoveStartPosition(Player player);

	public OptionalInt supplyMoveTargetPosition(Player player, int start);

}