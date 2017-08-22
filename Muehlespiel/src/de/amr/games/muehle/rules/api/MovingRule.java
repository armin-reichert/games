package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.player.api.Player;

public interface MovingRule {

	public String getDescription();

	public OptionalInt supplyStartPosition(Player player);

	public OptionalInt supplyTargetPosition(Player player, int from);

}