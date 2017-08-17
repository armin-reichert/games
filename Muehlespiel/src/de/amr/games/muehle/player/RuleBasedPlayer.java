package de.amr.games.muehle.player;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.util.Util.randomElement;
import static java.lang.String.format;

import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.PlacingRule;

/**
 * A player controlled by placing and (TODO) moving / removing rules.
 * 
 * @author Armin Reichert
 */
public class RuleBasedPlayer implements Player {

	private final Board board;
	private final StoneColor color;
	private final PlacingRule[] placingRules;

	private Move move;

	public RuleBasedPlayer(Board board, StoneColor color, PlacingRule[] placingRules) {
		this.board = board;
		this.color = color;
		this.placingRules = placingRules;
		move = new Move();
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	private OptionalInt tryRule(PlacingRule rule) {
		OptionalInt optPosition = rule.supplyPosition(board, color);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	@Override
	public OptionalInt supplyPlacePosition() {
		return Stream.of(placingRules).map(this::tryRule).filter(OptionalInt::isPresent).findFirst().get();
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
			OptionalInt millClosingFrom = randomElement(board.positions(color).filter(p -> board.canCloseMillFrom(p, color)));
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
			LOG.info(format("Ziehe Stein zu Position %d, weil eigene Mühle geschlossen wird", millClosingPos.getAsInt()));
			return millClosingPos;
		}
		return randomElement(board.emptyNeighbors(move.from));
	}
}