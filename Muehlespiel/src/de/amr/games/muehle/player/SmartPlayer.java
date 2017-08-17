package de.amr.games.muehle.player;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.player.PlacingRule.CLOSE_OWN_MILL;
import static de.amr.games.muehle.player.PlacingRule.DESTROY_OPPONENT_MILL;
import static de.amr.games.muehle.player.PlacingRule.FREE_POSITION_NEARBY_OWN_COLOR;
import static de.amr.games.muehle.player.PlacingRule.OPEN_OWN_MILL;
import static de.amr.games.muehle.player.PlacingRule.OPEN_TWO_OWN_MILLS;
import static de.amr.games.muehle.player.PlacingRule.RANDOM_FREE_POSITION;
import static de.amr.games.muehle.player.PlacingRule.RANDOM_POSITION_BOARD_EMPTY;
import static de.amr.games.muehle.util.Util.randomElement;
import static java.lang.String.format;

import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;

/**
 * A smart player.
 * 
 * @author Armin Reichert
 */
public class SmartPlayer implements Player {

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

	private final Board board;
	private final StoneColor color;
	private Move move;

	public SmartPlayer(Board board, StoneColor color) {
		this.board = board;
		this.color = color;
		move = new Move();
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	private OptionalInt tryRule(PlacingRule rule) {
		OptionalInt optPosition = rule.getRule().supplyPosition(board, color);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getRule().getReason(), pos)));
		return optPosition;
	}

	@Override
	public OptionalInt supplyPlacePosition() {
		return Stream.of(PLACING_RULES).map(this::tryRule).filter(OptionalInt::isPresent).findFirst().get();
	}

	@Override
	public OptionalInt supplyRemovalPosition(StoneColor otherColor) {
		return randomElement(board.positions(otherColor));
	}

	@Override
	public void clearMove() {
		move = new Move();
	}

	@Override
	public Move supplyMove(boolean canJump) {
		if (move.from == -1) {
			// Finde eine Position, von der aus eine Mühle geschlossen werden kann
			OptionalInt millClosingFrom = randomElement(board.positions(color).filter(p -> canCloseMillFrom(p)));
			millClosingFrom.ifPresent(p -> move.from = p);
			if (move.from == -1) {
				// Fallback
				OptionalInt moveStartPos = randomElement(board.positions(color).filter(board::hasEmptyNeighbor));
				moveStartPos.ifPresent(p -> move.from = p);
			}
		} else {
			OptionalInt moveEndPos = supplyMoveEndPosition();
			moveEndPos.ifPresent(p -> move.to = p);
		}
		return move;
	}

	private OptionalInt supplyMoveEndPosition() {
		// Suche freie Position, an der Mühle geschlossen werden kann:
		OptionalInt millClosingPos = randomElement(
				board.positions().filter(board::isEmptyPosition).filter(to -> board.isMillClosedByMove(move.from, to, color)));
		if (millClosingPos.isPresent()) {
			LOG.info(format("Ziehe Stein zu Position %d, weil eigene Mühle geschlossen wird", millClosingPos));
			return millClosingPos;
		}
		return randomElement(board.emptyNeighbors(move.from));
	}

	private boolean canCloseMillFrom(int p) {
		return board.neighbors(p).anyMatch(q -> board.isMillClosingPosition(q, color));
	}
}