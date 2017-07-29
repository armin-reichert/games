package de.amr.games.muehle;

import static de.amr.easy.game.Application.LOG;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.function.DoubleSupplier;

import de.amr.easy.game.math.Vector2;

public class Move {

	private final Board board;
	private final DoubleSupplier speedSupplier;

	private int from;
	private int to;
	private boolean running;
	private boolean complete;

	public Move(Board board, DoubleSupplier speedSupplier) {
		this.board = board;
		this.speedSupplier = speedSupplier;
		reset();
	}

	public void reset() {
		from = -1;
		to = -1;
		running = false;
		complete = false;
	}

	public void setFrom(int p) {
		from = p;
	}

	public int getFrom() {
		return from;
	}

	public void setTo(int p) {
		to = p;
	}

	public int getTo() {
		return to;
	}

	public boolean isComplete() {
		return complete;
	}

	public void run() {
		Stone stone = board.getStoneAt(from);
		if (!running) {
			stone.tf.setVelocity(computeVelocity());
			LOG.info("Starting move from " + from + " to " + to + " towards " + board.getDirection(from, to));
			running = true;
		}
		stone.tf.move();
		if (isEndPositionReached()) {
			running = false;
			complete = true;
			board.moveStone(from, to);
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