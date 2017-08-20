package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.player.api.Player;

public interface MoveStartRule {

	public OptionalInt supplyPosition(Player player);

	public String getDescription();
}