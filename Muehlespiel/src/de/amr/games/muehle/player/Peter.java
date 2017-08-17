package de.amr.games.muehle.player;

import static de.amr.games.muehle.rules.MoveStartRule.CAN_CLOSE_MILL_FROM;
import static de.amr.games.muehle.rules.MoveStartRule.HAS_EMPTY_NEIGHBOR;
import static de.amr.games.muehle.rules.MoveTargetRule.CLOSE_MILL;
import static de.amr.games.muehle.rules.MoveTargetRule.RANDOM_NEIGHBOR;
import static de.amr.games.muehle.rules.PlacingRule.CLOSE_OWN_MILL;
import static de.amr.games.muehle.rules.PlacingRule.DESTROY_OPPONENT_MILL;
import static de.amr.games.muehle.rules.PlacingRule.FREE_POSITION_NEARBY_OWN_COLOR;
import static de.amr.games.muehle.rules.PlacingRule.OPEN_OWN_MILL;
import static de.amr.games.muehle.rules.PlacingRule.OPEN_TWO_OWN_MILLS;
import static de.amr.games.muehle.rules.PlacingRule.RANDOM_FREE_POSITION;
import static de.amr.games.muehle.rules.PlacingRule.RANDOM_POSITION_BOARD_EMPTY;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.MoveStartRule;
import de.amr.games.muehle.rules.MoveTargetRule;
import de.amr.games.muehle.rules.PlacingRule;

public class Peter extends RuleBasedPlayer {

	private static final PlacingRule[] PLACING_RULES = {
			/*@formatter:off*/
			RANDOM_POSITION_BOARD_EMPTY,
			CLOSE_OWN_MILL,
			DESTROY_OPPONENT_MILL, 
			OPEN_TWO_OWN_MILLS, 
			OPEN_OWN_MILL, 
			FREE_POSITION_NEARBY_OWN_COLOR, 
			RANDOM_FREE_POSITION,
			/*@formatter:on*/
	};

	private static final MoveStartRule[] MOVE_START_RULES = {
			/*@formatter:off*/
			CAN_CLOSE_MILL_FROM,
			HAS_EMPTY_NEIGHBOR,
			/*@formatter:on*/
	};

	private static final MoveTargetRule[] MOVE_TARGET_RULES = {
			/*@formatter:off*/
			CLOSE_MILL,
			RANDOM_NEIGHBOR,
			/*@formatter:on*/
	};

	public Peter(Board board, StoneColor color) {
		super(board, color, PLACING_RULES, MOVE_START_RULES, MOVE_TARGET_RULES);
	}
}