package de.amr.games.muehle;

import static de.amr.games.muehle.Move.MoveState.COMPLETE;
import static de.amr.games.muehle.Move.MoveState.KNOWS_FROM;
import static de.amr.games.muehle.Move.MoveState.READY;
import static de.amr.games.muehle.Move.MoveState.RUNNING;

import java.awt.geom.Ellipse2D;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import de.amr.easy.game.Application;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.statemachine.StateMachine;

public class Move {

	public IntSupplier startPositionSupplier;
	public Supplier<Direction> directionSupplier;

	private final Board board;
	private final float speed;

	private int from;
	private int to;
	private Direction direction;

	// State machine

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
				direction = board.getDirection(from, to);
			});

			change(KNOWS_FROM, RUNNING, () -> to != -1);

			state(RUNNING).entry = s -> getStone().tf.setVelocity(computeVelocity());

			state(RUNNING).update = s -> getStone().tf.move();

			change(RUNNING, COMPLETE, () -> isEndPositionReached());

			state(RUNNING).exit = s -> board.moveStone(from, to);
		}
	}

	public Move(Board board, float speed) {
		this.board = board;
		this.speed = speed;
		startPositionSupplier = () -> -1;
		directionSupplier = () -> null;
		reset();
	}

	private void reset() {
		from = -1;
		to = -1;
		direction = null;
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

	public Direction getDirection() {
		return direction;
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
			direction = null;
		}
	}

	private void computeTo(Direction direction) {
		this.direction = direction;
		if (direction != null) {
			int targetPosition = board.findNeighbor(from, direction);
			if (targetPosition != -1 && !board.hasStoneAt(targetPosition)) {
				to = targetPosition;
			}
		}
	}

	private boolean isEndPositionReached() {
		Stone stone = board.getStoneAt(from);
		Ellipse2D center = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
		return center.contains(board.computeCenterPoint(to));
	}

	private Vector2 computeVelocity() {
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