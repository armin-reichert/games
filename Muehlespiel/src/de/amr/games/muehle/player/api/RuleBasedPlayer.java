package de.amr.games.muehle.player.api;

import static de.amr.easy.game.Application.LOG;
import static java.lang.String.format;

import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.api.MoveStartRule;
import de.amr.games.muehle.rules.api.MoveTargetRule;
import de.amr.games.muehle.rules.api.PlacingRule;
import de.amr.games.muehle.rules.api.RemovalRule;

/**
 * A player controlled by rules.
 * 
 * @author Armin Reichert
 */
public class RuleBasedPlayer implements Player {

	private final Board board;
	private final StoneColor color;
	private final PlacingRule[] placingRules;
	private final MoveStartRule[] moveStartRules;
	private final MoveTargetRule[] moveTargetRules;
	private final RemovalRule[] removalRules;

	private Move move;

	public RuleBasedPlayer(Board board, StoneColor color, PlacingRule[] placingRules, MoveStartRule[] moveStartRules,
			MoveTargetRule[] moveTargetRules, RemovalRule[] removalRules) {
		this.board = board;
		this.color = color;
		this.placingRules = placingRules;
		this.moveStartRules = moveStartRules;
		this.moveTargetRules = moveTargetRules;
		this.removalRules = removalRules;
		move = new Move();
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	@Override
	public void newMove() {
		move = new Move();
	}

	@Override
	public OptionalInt supplyPlacingPosition() {
		return Stream.of(placingRules).map(this::tryPlacingRule).filter(OptionalInt::isPresent).findFirst()
				.orElse(OptionalInt.empty());
	}

	@Override
	public OptionalInt supplyRemovalPosition() {
		return Stream.of(removalRules).map(rule -> tryRemovalRule(rule, color.other())).filter(OptionalInt::isPresent)
				.findFirst().orElse(OptionalInt.empty());
	}

	@Override
	public Move supplyMove(boolean canJump) {
		if (move.from == -1) {
			Stream.of(moveStartRules).map(this::tryMoveStartRule).filter(OptionalInt::isPresent).findFirst()
					.orElse(OptionalInt.empty()).ifPresent(pos -> move.from = pos);
		} else {
			supplyMoveEndPosition().ifPresent(pos -> move.to = pos);
		}
		return move;
	}

	private OptionalInt supplyMoveEndPosition() {
		return Stream.of(moveTargetRules).map(rule -> tryMoveTargetRule(rule, move.from)).filter(OptionalInt::isPresent)
				.findFirst().orElse(OptionalInt.empty());
	}

	private OptionalInt tryPlacingRule(PlacingRule rule) {
		OptionalInt optPosition = rule.selectPosition(board, color);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	private OptionalInt tryRemovalRule(RemovalRule rule, StoneColor removalColor) {
		OptionalInt optPosition = rule.selectPosition(board, removalColor);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	private OptionalInt tryMoveStartRule(MoveStartRule rule) {
		OptionalInt optPosition = rule.selectPosition(board, color);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	private OptionalInt tryMoveTargetRule(MoveTargetRule rule, int from) {
		OptionalInt optPosition = rule.selectPosition(board, color, from);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}
}