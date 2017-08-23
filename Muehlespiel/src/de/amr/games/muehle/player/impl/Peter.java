package de.amr.games.muehle.player.impl;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.MovingRule;
import de.amr.games.muehle.rules.api.PlacingRule;
import de.amr.games.muehle.rules.api.RemovalRule;
import de.amr.games.muehle.rules.impl.MovingRules;
import de.amr.games.muehle.rules.impl.PlacingRules;
import de.amr.games.muehle.rules.impl.RemovalRules;

public class Peter extends RuleBasedPlayer {

	static final PlacingRule[] PLACING_RULES = {
			/*@formatter:off*/
			PlacingRules.FIRST_STONE_RANDOM,
			PlacingRules.CLOSE_MILL,
//			PlacingRules.DESTROY_MILL, 
//			PlacingRules.OPEN_TWO_MILLS,
			PlacingRules.OPEN_ONE_MILL,
			PlacingRules.NEAR_OWN_COLOR, 
			PlacingRules.RANDOM,
			/*@formatter:on*/
	};

	static final MovingRule[] MOVING_RULES = {
			/*@formatter:off*/
			MovingRules.CAN_CLOSE_MILL,
			MovingRules.RANDOM,
			/*@formatter:on*/
	};

	static final RemovalRule[] REMOVAL_RULES = {
			/*@formatter:off*/
			RemovalRules.RANDOM,
			/*@formatter:on*/
	};

	@Override
	public String getName() {
		return "Strack";
	}

	public Peter(Board board, StoneColor color) {
		super(board, color, PLACING_RULES, MOVING_RULES, REMOVAL_RULES);
	}
}