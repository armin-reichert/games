package de.amr.games.muehle;

import static de.amr.games.muehle.Move.MoveState.COMPLETE;
import static de.amr.games.muehle.Move.MoveState.KNOWS_FROM;
import static de.amr.games.muehle.Move.MoveState.READY;
import static de.amr.games.muehle.Move.MoveState.RUNNING;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import de.amr.easy.game.Application;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.statemachine.StateMachine;

public class Move {

	public DoubleSupplier speedSupplier;
	public IntSupplier startPositionSupplier;
	public Supplier<Direction> directionSupplier;

	private final Board board;

	private int from;
	private int to;

	// State machine for controlling the move phases

	private final MoveControl control = new MoveControl();

	public enum MoveState {
		READY, KNOWS_FROM, RUNNING, COMPLETE
	};

	private class MoveControl extends StateMachine<MoveState, String> {

		public MoveControl() {
			super("Move Control", MoveState.class, READY);

			state(READY).entry = s -> reset();

			change(READY, KNOWS_FROM, () -> startPositionSupplier.getAsInt() != -1, (s, t) -> {
				setFrom(startPositionSupplier.getAsInt());
			});

			change(READY, KNOWS_FROM, () -> from != -1);

			state(KNOWS_FROM).update = s -> computeTo(directionSupplier.get());

			change(KNOWS_FROM, RUNNING, () -> board.emptyNeighbors(from).count() == 1, (s, t) -> {
				to = board.emptyNeighbors(from).findFirst().getAsInt();
			});

			change(KNOWS_FROM, RUNNING, () -> to != -1);

			state(RUNNING).entry = s -> getStone().tf.setVelocity(computeVelocity());

			state(RUNNING).update = s -> getStone().tf.move();

			change(RUNNING, COMPLETE, () -> isEndPositionReached());

			state(RUNNING).exit = s -> board.moveStone(from, to);
		}
	}

	public Move(Board board) {
		this.board = board;
		startPositionSupplier = () -> -1;
		directionSupplier = () -> null;
		speedSupplier = () -> 3f;
		reset();
	}

	private void reset() {
		from = -1;
		to = -1;
	}

	public void init() {
		control.init();
		control.setLogger(Application.LOG);
	}

	public void update() {
		control.update();
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public Stone getStone() {
		return board.getStoneAt(from);
	}

	public boolean isComplete() {
		return control.is(COMPLETE);
	}

	// private methods

	private void setFrom(int from) {
		if (this.from != from) {
			this.from = from;
			to = -1;
		}
	}

	private void computeTo(Direction direction) {
		if (direction != null) {
			int targetPosition = board.findNeighbor(from, direction);
			if (targetPosition != -1 && !board.hasStoneAt(targetPosition)) {
				to = targetPosition;
			}
		}
	}

	private boolean isEndPositionReached() {
		Stone stone = board.getStoneAt(from);
		Vector2 center = board.centerPoint(to);
		Vector2 velocity = new Vector2(stone.tf.getVelocityX(), stone.tf.getVelocityY());
		float speed = velocity.length();
		Ellipse2D spot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
		return spot.contains(new Point(center.roundedX(), center.roundedY()));
	}

	private Vector2 computeVelocity() {
		Direction direction = board.getDirection(from, to);
		float speed = (float) speedSupplier.getAsDouble();
		switch (direction) {
		case NORTH:
			return new Vector2(0, -speed);
		case EAST:
			return new Vector2(speed, 0);
		case SOUTH:
			return new Vector2(0, speed);
		case WEST:
			return new Vector2(-speed, 0);
		default:
			return new Vector2(0, 0);
		}
	}
}