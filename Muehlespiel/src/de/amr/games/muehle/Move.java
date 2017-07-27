package de.amr.games.muehle;

import java.awt.geom.Ellipse2D;

import de.amr.easy.game.math.Vector2;

public class Move {

	private final Board board;
	private final float speed;

	private int from;
	private int to;
	private Direction direction;
	private Vector2 velocity;

	public Move(Board board, float speed) {
		this.board = board;
		this.speed = speed;
		from = -1;
		to = -1;
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

	public void setFrom(int from) {
		if (this.from != from) {
			this.from = from;
			to = -1;
			direction = null;
			velocity = null;
		}
	}

	public void setDirection(Direction direction) {
		if (this.direction != direction) {
			this.direction = direction;
			if (direction != null) {
				computeEndPosition();
				computeVelocity();
			}
		}
	}

	private void computeEndPosition() {
		int targetPosition = board.findNeighbor(from, direction);
		if (targetPosition != -1 && !board.hasStoneAt(targetPosition)) {
			to = targetPosition;
		}
	}

	public boolean isEndPositionReached() {
		Ellipse2D center = new Ellipse2D.Float(board.getStoneAt(from).tf.getX() - 2, board.getStoneAt(from).tf.getY() - 2,
				4, 4);
		return center.contains(board.computeCenterPoint(to));
	}

	public void execute() {
		getStone().tf.setVelocity(velocity);
		getStone().tf.move();
	}

	private void computeVelocity() {
		switch (direction) {
		case NORTH:
			velocity = new Vector2(0, -speed);
			break;
		case EAST:
			velocity = new Vector2(speed, 0);
			break;
		case SOUTH:
			velocity = new Vector2(0, speed);
			break;
		case WEST:
			velocity = new Vector2(-speed, 0);
			break;
		}
	}
}