package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.player.api.Player;

public interface MoveTargetRule {

	public OptionalInt supplyPosition(Player player, int from);

	public String getDescription();
}