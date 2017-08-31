package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.player.api.Player;

public interface PlacingRule extends Rule {

	OptionalInt supplyPlacingPosition(Player player);
}
