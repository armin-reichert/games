package de.amr.games.muehle.rules.api;

import java.util.OptionalInt;

import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;

public interface RemovalRule extends Rule {

	OptionalInt supplyRemovalPosition(Player player, StoneColor removalColor);
}
