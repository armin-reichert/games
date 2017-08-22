package de.amr.games.muehle.player.impl;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.MovingRule;
import de.amr.games.muehle.rules.api.PlacingRule;
import de.amr.games.muehle.rules.api.RemovalRule;
import de.amr.games.muehle.rules.impl.MovingRules;
import de.amr.games.muehle.rules.impl.PlacingRules;
import de.amr.games.muehle.rules.impl.RemovalRules;

/**
 * A player acting randomly.
 * 
 * @author Armin Reichert
 */
public class RandomPlayer extends RuleBasedPlayer {

	public RandomPlayer(Board board, StoneColor color) {
		super(board, color, new PlacingRule[] { PlacingRules.RANDOM }, new MovingRule[] { MovingRules.RANDOM },
				new RemovalRule[] { RemovalRules.RANDOM });
	}
}