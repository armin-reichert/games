package de.amr.games.muehle.player;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.util.Util.randomElement;
import static java.lang.String.format;

import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.MoveStartRule;
import de.amr.games.muehle.rules.MoveTargetRule;
import de.amr.games.muehle.rules.PlacingRule;
import de.amr.games.muehle.rules.PositionSelectionRule;

/**
 * A player controlled by placing and (TODO) moving / removing rules.
 * 
 * @author Armin Reichert
 */
public class RuleBasedPlayer implements Player {

	private final Board board;
	private final StoneColor color;
	private final PlacingRule[] placingRules;
	private final MoveStartRule[] moveStartRules;
	private final MoveTargetRule[] moveTargetRules;

	private Move move;

	public RuleBasedPlayer(Board board, StoneColor color, PlacingRule[] placingRules, MoveStartRule[] moveStartRules,
			MoveTargetRule[] moveTargetRules) {
		this.board = board;
		this.color = color;
		this.placingRules = placingRules;
		this.moveStartRules = moveStartRules;
		this.moveTargetRules = moveTargetRules;
		move = new Move();
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	private OptionalInt tryPlacingOrMoveStartRule(PositionSelectionRule rule) {
		OptionalInt optPosition = rule.selectPosition(board, color);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	private OptionalInt tryMoveTargetRule(MoveTargetRule rule, int from) {
		OptionalInt optPosition = rule.selectPosition(board, color, from);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	@Override
	public OptionalInt supplyPlacingPosition() {
		return Stream.of(placingRules).map(this::tryPlacingOrMoveStartRule).filter(OptionalInt::isPresent).findFirst().get();
	}

	@Override
	public OptionalInt supplyRemovalPosition(StoneColor otherColor) {
		return randomElement(board.positions(otherColor));
	}

	@Override
	public Move supplyMove(boolean canJump) {
		if (move.from == -1) {
			Stream.of(moveStartRules).map(this::tryPlacingOrMoveStartRule).filter(OptionalInt::isPresent).findFirst()
					.ifPresent(optPos -> optPos.ifPresent(pos -> move.from = pos));
		} else {
			supplyMoveEndPosition().ifPresent(p -> move.to = p);
		}
		return move;
	}

	private OptionalInt supplyMoveEndPosition() {
		return Stream.of(moveTargetRules).map(rule -> tryMoveTargetRule(rule, move.from)).filter(OptionalInt::isPresent)
				.findFirst().orElse(OptionalInt.empty());
	}

	@Override
	public void clearMove() {
		move = new Move();
	}
}