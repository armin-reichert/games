package de.amr.games.muehle.player;

import static de.amr.easy.game.Application.LOG;
import static java.lang.String.format;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.rules.MovingRule;
import de.amr.games.muehle.rules.PlacingRule;
import de.amr.games.muehle.rules.RemovalRule;

/**
 * A player controlled by rules.
 * 
 * @author Armin Reichert
 */
public abstract class RuleBasedPlayer implements Player {

	private final Board board;
	private final StoneColor color;
	private final PlacingRule[] placingRules;
	private final MovingRule[] movingRules;
	private final RemovalRule[] removalRules;

	public RuleBasedPlayer(Board board, StoneColor color, PlacingRule[] placingRules, MovingRule[] movingRules,
			RemovalRule[] removalRules) {
		this.board = board;
		this.color = color;
		this.placingRules = placingRules;
		this.movingRules = movingRules;
		this.removalRules = removalRules;
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
	public Optional<Move> supplyMove() {
		return Stream.of(movingRules).map(this::tryMoveRule).filter(Optional::isPresent).findFirst()
				.orElse(Optional.empty());
	}

	@Override
	public void newMove() {
	}

	private OptionalInt tryPlacingRule(PlacingRule rule) {
		OptionalInt optPos = rule.supplyPlacingPosition(this);
		optPos.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPos;
	}

	private OptionalInt tryRemovalRule(RemovalRule rule) {
		OptionalInt optPos = rule.supplyRemovalPosition(this, getColor().other());
		optPos.ifPresent(pos -> LOG.info(getName() + ": " + format(rule.getDescription(), pos)));
		return optPos;
	}

	private Optional<Move> tryMoveRule(MovingRule rule) {
		Optional<Move> optMove = rule.supplyMove(this);
		optMove.ifPresent(move -> LOG.info(getName() + ": " + format(rule.getDescription(), move.from, move.to)));
		return optMove;
	}
}