package de.amr.games.muehle.rules;

import java.util.OptionalInt;

import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.Player;

public interface RemovalRule extends Rule {

	OptionalInt supplyRemovalPosition(Player player, StoneColor removalColor);
}
