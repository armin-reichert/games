package de.amr.games.muehle.player.impl;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.MovingRule;
import de.amr.games.muehle.rules.api.PlacingRule;
import de.amr.games.muehle.rules.api.RemovalRule;
import de.amr.games.muehle.rules.impl.MovingRules;
import de.amr.games.muehle.rules.impl.PlacingRules;
import de.amr.games.muehle.rules.impl.RemovalRules;

public class Zwick extends RuleBasedPlayer {

	static final PlacingRule[] PLACING_RULES = {
			/*@formatter:off*/
			PlacingRules.EMPTYBOARD,
			PlacingRules.CLOSE_MILL,
			PlacingRules.DESTROY_MILL, 
			PlacingRules.OPEN_TWO_MILLS,
			PlacingRules.OPEN_ONE_MILL,
			PlacingRules.NEAR_OWN_COLOR, 
			PlacingRules.RANDOM,
			/*@formatter:on*/
	};

	static final MovingRule[] MOVING_RULES = {
			/*@formatter:off*/
			MovingRules.CAN_CLOSE_MILL,
			MovingRules.ANY_POSSIBLE_MOVE,
			/*@formatter:on*/
	};

	static final RemovalRule[] REMOVAL_RULES = {
			/*@formatter:off*/
			RemovalRules.RANDOM,
			/*@formatter:on*/
	};

	@Override
	public String getName() {
		return "Zwicki";
	}

	public Zwick(Board board, StoneColor color) {
		super(board, color, PLACING_RULES, MOVING_RULES, REMOVAL_RULES);
	}
}