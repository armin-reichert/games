package de.amr.games.muehle.player.impl;

import static de.amr.easy.game.Application.LOG;
import static java.lang.String.format;

import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.rules.api.MovingRule;
import de.amr.games.muehle.rules.api.PlacingRule;
import de.amr.games.muehle.rules.api.RemovalRule;
import de.amr.games.muehle.rules.api.Rule;

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
		return Stream.of(movingRules).map(this::tryMoveTargetRule).filter(OptionalInt::isPresent).findFirst()
				.orElse(OptionalInt.empty());
	}

	OptionalInt tryPlacingRule(PlacingRule rule) {
		return logMatch(rule, rule.supplyPlacingPosition(this));
	}

	OptionalInt tryRemovalRule(RemovalRule rule) {
		return logMatch(rule, rule.supplyRemovalPosition(this, getColor().other()));
	}

	OptionalInt tryMoveStartRule(MovingRule rule) {
		return logMatch(rule, rule.supplyMoveStartPosition(this));
	}

	OptionalInt tryMoveTargetRule(MovingRule rule) {
		return logMatch(rule, rule.supplyMoveTargetPosition(this, move.from));
	}

	OptionalInt logMatch(Rule rule, OptionalInt optPos) {
		optPos.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPos;
	}
}