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
	private final BooleanSupplier canJumpSupplier;
	private final DoubleSupplier speedSupplier;
	private final MoveControl control;

	private int from;
	private int to;
	private Vector2 toCoordinate;
	private Stone affectedStone;

	public enum MoveState {
		INITIAL, KNOWS_FROM, KNOWS_TO, MOVING, JUMPING, COMPLETE;
	};

	private class MoveControl extends StateMachine<MoveState, Object> {

		public MoveControl() {
			super("Move Control", MoveState.class, INITIAL);

			state(INITIAL).entry = s -> clear();

			change(INITIAL, KNOWS_FROM, () -> from != -1);

			change(KNOWS_FROM, KNOWS_TO, () -> to != -1);

			change(KNOWS_TO, MOVING, () -> board.getModel().areNeighbors(from, to));

			change(KNOWS_TO, JUMPING, canJumpSupplier::getAsBoolean);

			state(MOVING).entry = s -> {
				affectedStone = board.getStoneAt(from).get();
				Direction dir = board.getModel().getDirection(from, to).get();
				affectedStone.tf.setVelocity(supplyVelocity(dir));
				LOG.info(format("Starting move from %d to %d towards %s", from, to, dir));
			};

			state(MOVING).update = s -> affectedStone.tf.move();

			change(MOVING, COMPLETE, () -> isEndPositionReached());

			state(JUMPING).entry = s -> {
				affectedStone = board.getStoneAt(from).get();
				LOG.info(format("Jumping from %d to %d", from, to));
			};

			change(JUMPING, COMPLETE);

			state(COMPLETE).entry = s -> {
				affectedStone.tf.setVelocity(0, 0);
				board.moveStone(from, to);
			};
		}
	}

	public Move(Board board, DoubleSupplier speedSupplier, BooleanSupplier canJumpSupplier) {
		this.board = board;
		this.speedSupplier = speedSupplier;
		this.canJumpSupplier = canJumpSupplier;
		control = new MoveControl();
		control.setLogger(LOG);
		init();
	}

	public void init() {
		control.init();
	}

	public void update() {
		control.update();
	}

	public void setFrom(int p) {
		from = p;
		control.update();
	}

	public OptionalInt getFrom() {
		return from == -1 ? OptionalInt.empty() : OptionalInt.of(from);
	}

	public void setTo(int p) {
		to = p;
		toCoordinate = board.centerPoint(to);
		control.update();
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
		affectedStone = null;
	}

	private boolean isEndPositionReached() {
		float speed = affectedStone.tf.getVelocity().length();
		Ellipse2D targetSpot = new Ellipse2D.Float(affectedStone.tf.getX() - speed, affectedStone.tf.getY() - speed,
				2 * speed, 2 * speed);
		return targetSpot.contains(new Point(toCoordinate.roundedX(), toCoordinate.roundedY()));
	}

	private Vector2 supplyVelocity(Direction dir) {
		Vector2 v = Vector2.nullVector();
		float speed = (float) speedSupplier.getAsDouble();
		switch (dir) {
		case NORTH:
			v = new Vector2(0, -speed);
			break;
		case EAST:
			v = new Vector2(speed, 0);
			break;
		case SOUTH:
			v = new Vector2(0, speed);
			break;
		case WEST:
			v = new Vector2(-speed, 0);
			break;
		}
		return v;
	}
}