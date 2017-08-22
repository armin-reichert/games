package de.amr.games.muehle.player.impl;

import static de.amr.easy.game.Application.LOG;
import static java.lang.String.format;

import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.MovingRule;
import de.amr.games.muehle.rules.api.PlacingRule;
import de.amr.games.muehle.rules.api.RemovalRule;

/**
 * A player controlled by rules.
 * 
 * @author Armin Reichert
 */
public class RuleBasedPlayer implements Player {

	final Board board;
	final StoneColor color;
	final PlacingRule[] placingRules;
	final MovingRule[] movingRules;
	final RemovalRule[] removalRules;

	Move move;

	public RuleBasedPlayer(Board board, StoneColor color, PlacingRule[] placingRules, MovingRule[] movingRules,
			RemovalRule[] removalRules) {
		this.board = board;
		this.color = color;
		this.placingRules = placingRules;
		this.movingRules = movingRules;
		this.removalRules = removalRules;
		move = new Move();
	}

	@Override
	public Board getBoard() {
		return board;
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
		return Stream.of(removalRules).map(this::tryRemovalRule).filter(OptionalInt::isPresent).findFirst()
				.orElse(OptionalInt.empty());
	}

	@Override
	public Move supplyMove() {
		if (move.from == -1) {
			supplyMoveStartPosition().ifPresent(pos -> move.from = pos);
		} else {
			supplyMoveEndPosition().ifPresent(pos -> move.to = pos);
		}
		return move;
	}

	OptionalInt supplyMoveStartPosition() {
		return Stream.of(movingRules).map(this::tryMoveStartRule).filter(OptionalInt::isPresent).findFirst()
				.orElse(OptionalInt.empty());
	}

	OptionalInt supplyMoveEndPosition() {
		return Stream.of(movingRules).map(rule -> tryMoveTargetRule(rule, move.from)).filter(OptionalInt::isPresent)
				.findFirst().orElse(OptionalInt.empty());
	}

	OptionalInt tryPlacingRule(PlacingRule rule) {
		OptionalInt optPosition = rule.supplyPosition(this);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	OptionalInt tryRemovalRule(RemovalRule rule) {
		OptionalInt optPosition = rule.supplyPosition(this, getColor().other());
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	OptionalInt tryMoveStartRule(MovingRule rule) {
		OptionalInt optPosition = rule.supplyStartPosition(this);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}

	OptionalInt tryMoveTargetRule(MovingRule rule, int from) {
		OptionalInt optPosition = rule.supplyTargetPosition(this, from);
		optPosition.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPosition;
	}
}