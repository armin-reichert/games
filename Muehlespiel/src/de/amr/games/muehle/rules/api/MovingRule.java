package de.amr.games.muehle.rules.api;

import java.util.Optional;

import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;

public interface MovingRule extends Rule {

	public Optional<Move> supplyMove(Player player);
}