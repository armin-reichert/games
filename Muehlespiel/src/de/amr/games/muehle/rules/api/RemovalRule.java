package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;

public interface RemovalRule {

	public OptionalInt supplyPosition(Player player, StoneColor removalColor);

	public String getDescription();
}
