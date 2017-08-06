package de.amr.games.muehle.ui;

import static de.amr.easy.game.Application.LOG;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.DoubleSupplier;

import de.amr.easy.game.math.Vector2;
import de.amr.games.muehle.board.Direction;

public class Move {

	private final Board boardEntity;
	private final DoubleSupplier speedSupplier;

	private int from;
	private int to;
	private boolean moving;
	private boolean complete;

	public Move(Board boardEntity, DoubleSupplier speedSupplier) {
		this.boardEntity = boardEntity;
		this.speedSupplier = speedSupplier;
		clear();
	}

	public void clear() {
		from = -1;
		to = -1;
		moving = false;
		complete = false;
	}

	public void setFrom(int p) {
		from = p;
	}

	public OptionalInt getFrom() {
		return from == -1 ? OptionalInt.empty() : OptionalInt.of(from);
	}

	public void setTo(int p) {
		to = p;
	}

	public OptionalInt getTo() {
		return to == -1 ? OptionalInt.empty() : OptionalInt.of(to);
	}

	public boolean isComplete() {
		return complete;
	}

	public void execute() {
		if (boardEntity.getBoardGraph().areNeighbors(from, to)) {
			move();
		} else {
			jump();
		}
	}

	private void move() {
		boardEntity.getStoneAt(from).ifPresent(stone -> {
			if (!moving) {
				supplyVelocity().ifPresent(velocity -> stone.tf.setVelocity(velocity));
				LOG.info("Starting move from " + from + " to " + to + " towards "
						+ boardEntity.getBoardGraph().getDirection(from, to));
				moving = true;
			}
			stone.tf.move();
			if (isEndPositionReached()) {
				stone.tf.setVelocity(0, 0);
				moving = false;
				complete = true;
				boardEntity.moveStone(from, to);
			}
		});
	}

	private void jump() {
		LOG.info("Jumping from " + from + " to " + to);
		boardEntity.moveStone(from, to);
		moving = false;
		complete = true;
	}

	private boolean isEndPositionReached() {
		Stone stone = boardEntity.getStoneAt(from).get();
		Vector2 center = boardEntity.centerPoint(to);
		Vector2 velocity = new Vector2(stone.tf.getVelocityX(), stone.tf.getVelocityY());
		float speed = velocity.length();
		Ellipse2D spot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
		return spot.contains(new Point(center.roundedX(), center.roundedY()));
	}

	private Optional<Vector2> supplyVelocity() {
		Optional<Direction> direction = boardEntity.getBoardGraph().getDirection(from, to);
		if (direction.isPresent()) {
			float speed = (float) speedSupplier.getAsDouble();
			switch (direction.get()) {
			case NORTH:
				return Optional.of(new Vector2(0, -speed));
			case EAST:
				return Optional.of(new Vector2(speed, 0));
			case SOUTH:
				return Optional.of(new Vector2(0, speed));
			case WEST:
				return Optional.of(new Vector2(-speed, 0));
			}
		}
		return Optional.empty();
	}
}