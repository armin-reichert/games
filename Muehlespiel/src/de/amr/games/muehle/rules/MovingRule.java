package de.amr.games.muehle.rules;

import java.util.Optional;

import de.amr.games.muehle.player.Move;
import de.amr.games.muehle.player.Player;

public interface MovingRule extends Rule {

	Optional<Move> supplyMove(Player player);
}