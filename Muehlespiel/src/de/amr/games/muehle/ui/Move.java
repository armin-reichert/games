package de.amr.games.muehle.ui;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.ui.Move.MoveState.COMPLETE;
import static de.amr.games.muehle.ui.Move.MoveState.INITIAL;
import static de.amr.games.muehle.ui.Move.MoveState.JUMPING;
import static de.amr.games.muehle.ui.Move.MoveState.KNOWS_FROM;
import static de.amr.games.muehle.ui.Move.MoveState.KNOWS_TO;
import static de.amr.games.muehle.ui.Move.MoveState.MOVING;
import static java.lang.String.format;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import de.amr.easy.game.math.Vector2;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.board.Direction;

/**
 * A move animation in the board UI.
 * 
 * @author Armin Reichert
 */
public class Move {

	private final Board board;
	private final Supplier<OptionalInt> fromSupplier;
	private final Supplier<OptionalInt> toSupplier;
	private final BooleanSupplier canJumpSupplier;
	private final DoubleSupplier speedSupplier;
	private final MoveControl control;

	private int from;
	private int to;
	private Vector2 toCoordinate;
	private Stone stone;

	/**
	 * Constructs an initial move.
	 * 
	 * @param board
	 *          the board where the move is executed
	 * @param fromSupplier
	 *          supplies the position from where the move starts
	 * @param toSupplier
	 *          supplies the position where the move ends
	 * @param speedSupplier
	 *          supplies the move speed
	 * @param canJumpSupplier
	 *          tells if the move may be a jump
	 */
	public Move(Board board, Supplier<OptionalInt> fromSupplier, Supplier<OptionalInt> toSupplier,
			DoubleSupplier speedSupplier, BooleanSupplier canJumpSupplier) {
		this.board = board;
		this.fromSupplier = fromSupplier;
		this.toSupplier = toSupplier;
		this.speedSupplier = speedSupplier;
		this.canJumpSupplier = canJumpSupplier;
		control = new MoveControl();
		control.setLogger(LOG);
		init();
	}

	public enum MoveState {
		INITIAL, KNOWS_FROM, KNOWS_TO, MOVING, JUMPING, COMPLETE;
	};

	private class MoveControl extends StateMachine<MoveState, Object> {

		public MoveControl() {
			super("Move Control", MoveState.class, INITIAL);

			state(INITIAL).entry = s -> clear();

			change(INITIAL, KNOWS_FROM, () -> fromSupplier.get().isPresent(), (s, t) -> from = fromSupplier.get().getAsInt());

			change(KNOWS_FROM, KNOWS_TO, () -> toSupplier.get().isPresent(), (s, t) -> to = toSupplier.get().getAsInt());

			change(KNOWS_TO, MOVING, () -> board.getModel().areNeighbors(from, to));

			change(KNOWS_TO, JUMPING, canJumpSupplier::getAsBoolean);

			state(MOVING).entry = s -> {
				Direction dir = board.getModel().getDirection(from, to).get();
				stone = board.getStoneAt(from).get();
				stone.tf.setVelocity(computeVelocity(dir));
				toCoordinate = board.centerPoint(to);
				LOG.info(format("Starting move from %d to %d towards %s", from, to, dir));
			};

			state(MOVING).update = s -> stone.tf.move();

			change(MOVING, COMPLETE, () -> isEndPositionReached());

			state(MOVING).exit = s -> stone.tf.setVelocity(0, 0);

			state(JUMPING).entry = s -> {
				stone = board.getStoneAt(from).get();
				LOG.info(format("Jumping from %d to %d", from, to));
			};

			change(JUMPING, COMPLETE);

			state(COMPLETE).entry = s -> board.moveStone(from, to);
		}
	}

	public void init() {
		control.init();
	}

	public void update() {
		control.update();
	}

	public OptionalInt getFrom() {
		return from == -1 ? OptionalInt.empty() : OptionalInt.of(from);
	}

	public OptionalInt getTo() {
		return to == -1 ? OptionalInt.empty() : OptionalInt.of(to);
	}

	public boolean isComplete() {
		return control.is(COMPLETE);
	}

	private void clear() {
		from = -1;
		to = -1;
		toCoordinate = null;
		stone = null;
	}

	private boolean isEndPositionReached() {
		float speed = stone.tf.getVelocity().length();
		Ellipse2D targetSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
		return targetSpot.contains(new Point(toCoordinate.roundedX(), toCoordinate.roundedY()));
	}

	private Vector2 computeVelocity(Direction dir) {
		float speed = (float) speedSupplier.getAsDouble();
		switch (dir) {
		case NORTH:
			return new Vector2(0, -speed);
		case EAST:
			return new Vector2(speed, 0);
		case SOUTH:
			return new Vector2(0, speed);
		case WEST:
			return new Vector2(-speed, 0);
		}
		return Vector2.nullVector();
	}
}