package de.amr.games.muehle.player.impl;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.MoveStartRule;
import de.amr.games.muehle.rules.api.MoveTargetRule;
import de.amr.games.muehle.rules.api.PlacingRule;
import de.amr.games.muehle.rules.api.RemovalRule;
import de.amr.games.muehle.rules.impl.MoveStartRules;
import de.amr.games.muehle.rules.impl.MoveTargetRules;
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

	static final MoveStartRule[] MOVE_START_RULES = {
			/*@formatter:off*/
			MoveStartRules.CAN_CLOSE_MILL,
			MoveStartRules.CAN_MOVE,
			/*@formatter:on*/
	};

	static final MoveTargetRule[] MOVE_TARGET_RULES = {
			/*@formatter:off*/
			MoveTargetRules.CLOSE_MILL,
			MoveTargetRules.RANDOM,
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
		super(board, color, PLACING_RULES, MOVE_START_RULES, MOVE_TARGET_RULES, REMOVAL_RULES);
		// TODO Auto-generated constructor stub
	}

}
