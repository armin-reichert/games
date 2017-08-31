package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.game.math.Vector2f.dist;
import static de.amr.games.muehle.play.MoveState.FINISHED;
import static de.amr.games.muehle.play.MoveState.INITIAL;
import static de.amr.games.muehle.play.MoveState.JUMPING;
import static de.amr.games.muehle.play.MoveState.MOVING;
import static de.amr.games.muehle.play.MoveState.READING_MOVE;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;

import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.timing.Pulse;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.ui.Stone;

/**
 * A finite state machine for controlling the movement of stones.
 * 
 * @author Armin Reichert
 */
public class MoveControl extends StateMachine<MoveState, Object> {

	static final float MOVE_TIME_SEC = 0.75f;

	private final Pulse pulse;
	private final MillGameUI gameUI;
	private final Board board;
	private final Player player;

	private int from, to;

	/**
	 * Constructs a move control.
	 * 
	 * @param player
	 *          the player making the move
	 * @param gameUI
	 *          the game UI where the move is visualized
	 * @param pulse
	 *          pulse driving the application
	 */
	public MoveControl(Board board, Player player, MillGameUI gameUI, Pulse pulse) {
		super("Move Control", MoveState.class, READING_MOVE);

		this.player = player;
		this.board = board;
		this.gameUI = gameUI;
		this.pulse = pulse;

		// INITIAL

		state(INITIAL).entry = this::newMove;

		change(INITIAL, READING_MOVE);

		// READING_MOVE

		state(READING_MOVE).update = this::readMove;

		change(READING_MOVE, INITIAL, () -> hasMoveStartPosition() && !isMoveStartPossible());

		change(READING_MOVE, INITIAL, () -> hasBothMovePositions() && !isMovePossible());

		change(READING_MOVE, JUMPING, () -> hasBothMovePositions() && isMovePossible() && player.canJump());

		change(READING_MOVE, MOVING, () -> hasBothMovePositions() && isMovePossible());

		// MOVING

		state(MOVING).entry = this::startMove;

		state(MOVING).update = this::moveStone;

		change(MOVING, FINISHED, this::isStoneAtTarget);

		state(MOVING).exit = this::stopMove;

		// JUMPING

		state(JUMPING).entry = s -> LOG.info(player.getName() + ": " + Messages.text("jumping_from_to", from, to));

		change(JUMPING, FINISHED);

		// FINISHED

		state(FINISHED).entry = s -> gameUI.moveStone(from, to);
	}

	void newMove(State state) {
		from = to = -1;
		player.newMove();
	}

	void readMove(State state) {
		player.supplyMove().ifPresent(move -> {
			from = move.from;
			to = move.to;
		});
	}

	void startMove(State state) {
		board.getDirection(from, to).ifPresent(dir -> {
			stone().ifPresent(stone -> stone.tf.setVelocity(velocity(dir)));
			LOG.info(player.getName() + ": " + Messages.text("moving_from_to_towards", from, to, dir));
		});
	}

	void stopMove(State state) {
		stone().ifPresent(stone -> stone.tf.setVelocity(0, 0));
	}

	void moveStone(State state) {
		stone().ifPresent(stone -> stone.tf.move());
	}

	public Optional<Move> getMove() {
		return from != -1 && to != -1 ? Optional.of(new Move(from, to)) : Optional.empty();
	}

	public boolean hasMoveStartPosition() {
		return board.isValidPosition(from);
	}

	boolean hasBothMovePositions() {
		return hasMoveStartPosition() && board.isValidPosition(to);
	}

	public boolean isMoveStartPossible() {
		return hasMoveStartPosition() && board.hasStoneAt(from) && board.getStoneAt(from).get() == player.getColor()
				&& (player.canJump() || board.hasEmptyNeighbor(from));
	}

	boolean isMovePossible() {
		if (!stone().isPresent()) {
			LOG.info(Messages.text("stone_at_position_not_existing", from));
		} else if (stone().get().getColor() != player.getColor()) {
			LOG.info(Messages.text("stone_at_position_wrong_color", from));
		} else if (!player.canJump() && !board.hasEmptyNeighbor(from)) {
			LOG.info(Messages.text("stone_at_position_cannot_move", from));
		} else if (board.isEmptyPosition(to) && (player.canJump() || board.areNeighbors(from, to))) {
			return player.canJump() ? board.isEmptyPosition(to) : board.isEmptyPosition(to) && board.areNeighbors(from, to);
		}
		return false;
	}

	boolean isStoneAtTarget() {
		if (stone().isPresent()) {
			Stone stone = stone().get();
			float speed = stone.tf.getVelocity().length();
			Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
			Vector2f toCenter = gameUI.centerPoint(to);
			return stoneSpot.contains(new Point2D.Float(toCenter.x, toCenter.y));
		}
		return false;
	}

	Optional<Stone> stone() {
		return gameUI.getStoneAt(from);
	}

	Vector2f velocity(Direction dir) {
		float speed = dist(gameUI.centerPoint(from), gameUI.centerPoint(to)) / pulse.secToTicks(MOVE_TIME_SEC);
		switch (dir) {
		case NORTH:
			return Vector2f.of(0, -speed);
		case EAST:
			return Vector2f.of(speed, 0);
		case SOUTH:
			return Vector2f.of(0, speed);
		case WEST:
			return Vector2f.of(-speed, 0);
		}
		throw new IllegalArgumentException("Illegal direction: " + dir);
	}
}