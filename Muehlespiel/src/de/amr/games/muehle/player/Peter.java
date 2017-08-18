package de.amr.games.muehle.player;

import static de.amr.games.muehle.rules.samples.MoveStartRules.CAN_CLOSE_MILL_FROM;
import static de.amr.games.muehle.rules.samples.MoveStartRules.HAS_EMPTY_NEIGHBOR;
import static de.amr.games.muehle.rules.samples.MoveTargetRules.CLOSE_MILL;
import static de.amr.games.muehle.rules.samples.MoveTargetRules.RANDOM_NEIGHBOR;
import static de.amr.games.muehle.rules.samples.PlacingRules.CLOSE_OWN_MILL;
import static de.amr.games.muehle.rules.samples.PlacingRules.DESTROY_OPPONENT_MILL;
import static de.amr.games.muehle.rules.samples.PlacingRules.FREE_POSITION_NEARBY_OWN_COLOR;
import static de.amr.games.muehle.rules.samples.PlacingRules.OPEN_OWN_MILL;
import static de.amr.games.muehle.rules.samples.PlacingRules.OPEN_TWO_OWN_MILLS;
import static de.amr.games.muehle.rules.samples.PlacingRules.RANDOM_FREE_POSITION;
import static de.amr.games.muehle.rules.samples.PlacingRules.RANDOM_POSITION_BOARD_EMPTY;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.samples.MoveStartRules;
import de.amr.games.muehle.rules.samples.MoveTargetRules;
import de.amr.games.muehle.rules.samples.PlacingRules;

public class Peter extends RuleBasedPlayer {

	private static final PlacingRules[] PLACING_RULES = {
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

	private static final MoveStartRules[] MOVE_START_RULES = {
			/*@formatter:off*/
			CAN_CLOSE_MILL_FROM,
			HAS_EMPTY_NEIGHBOR,
			/*@formatter:on*/
	};

	private static final MoveTargetRules[] MOVE_TARGET_RULES = {
			/*@formatter:off*/
			CLOSE_MILL,
			RANDOM_NEIGHBOR,
			/*@formatter:on*/
	};

	public Peter(Board board, StoneColor color) {
		super(board, color, PLACING_RULES, MOVE_START_RULES, MOVE_TARGET_RULES);
	}
}