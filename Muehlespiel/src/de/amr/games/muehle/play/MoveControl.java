package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.play.MoveState.FINISHED;
import static de.amr.games.muehle.play.MoveState.INITIAL;
import static de.amr.games.muehle.play.MoveState.JUMPING;
import static de.amr.games.muehle.play.MoveState.MOVING;
import static de.amr.games.muehle.play.MoveState.READING_MOVE;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;
import java.util.function.BiFunction;

import de.amr.easy.game.math.Vector2;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.api.Move;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.ui.BoardUI;
import de.amr.games.muehle.ui.Stone;

/**
 * A finite state machine for controlling the movement of stones.
 * 
 * @author Armin Reichert
 */
public class MoveControl extends StateMachine<MoveState, Object> {

	private final BiFunction<Integer, Integer, Float> moveSpeed; // ticks per second
	private final BoardUI boardUI;
	private final Board board;
	private Player player;
	private Move move;

	/**
	 * Constructs a move control.
	 * 
	 * @param boardUI
	 *          the board UI where the move is displayed
	 * @param moveSpeed
	 *          function computing the move speed between two points
	 */
	public MoveControl(BoardUI boardUI, BiFunction<Integer, Integer, Float> moveSpeed) {
		super("Move Control", MoveState.class, READING_MOVE);
		this.boardUI = boardUI;
		this.board = boardUI.board();
		this.moveSpeed = moveSpeed;
		defineStateMachine();
	}

	private void defineStateMachine() {

		// INITIAL

		state(INITIAL).entry = s -> {
			move = null;
			player.newMove();
		};

		change(INITIAL, READING_MOVE);

		// READING_MOVE

		state(READING_MOVE).update = s -> move = player.supplyMove();

		change(READING_MOVE, INITIAL, () -> hasMoveStartPosition() && !isMoveStartPossible());

		change(READING_MOVE, INITIAL, () -> hasBothMovePositions() && !isMovePossible());

		change(READING_MOVE, JUMPING, () -> hasBothMovePositions() && isMovePossible() && player.canJump());

		change(READING_MOVE, MOVING, () -> hasBothMovePositions() && isMovePossible());

		// MOVING

		state(MOVING).entry = s -> {
			board.getDirection(move.from, move.to).ifPresent(moveDir -> {
				stoneToMove().ifPresent(stone -> stone.tf.setVelocity(computeVelocity(moveDir)));
				LOG.info(Messages.text("moving_from_to_towards", move.from, move.to, moveDir));
			});
		};

		state(MOVING).update = s -> stoneToMove().ifPresent(stone -> stone.tf.move());

		change(MOVING, FINISHED, this::endPositionReached);

		state(MOVING).exit = s -> stoneToMove().ifPresent(stone -> stone.tf.setVelocity(0, 0));

		// JUMPING

		state(JUMPING).entry = s -> LOG.info(Messages.text("jumping_from_to", move.from, move.to));

		change(JUMPING, FINISHED);

		// FINISHED

		state(FINISHED).entry = s -> boardUI.moveStone(move.from, move.to);
	}

	public void controlPlayer(Player player) {
		this.player = player;
		init();
	}

	public Optional<Move> getMove() {
		return move == null ? Optional.empty() : Optional.of(move);
	}

	public boolean hasMoveStartPosition() {
		return move != null && board.isValidPosition(move.from);
	}

	private boolean hasBothMovePositions() {
		return hasMoveStartPosition() && board.isValidPosition(move.to);
	}

	public boolean isMoveStartPossible() {
		return hasMoveStartPosition() && board.hasStoneAt(move.from)
				&& board.getStoneAt(move.from).get() == player.getColor()
				&& (player.canJump() || board.hasEmptyNeighbor(move.from));
	}

	private boolean isMovePossible() {
		if (!stoneToMove().isPresent()) {
			LOG.info(Messages.text("stone_at_position_not_existing", move.from));
		} else if (stoneToMove().get().getColor() != player.getColor()) {
			LOG.info(Messages.text("stone_at_position_wrong_color", move.from));
		} else if (!player.canJump() && !board.hasEmptyNeighbor(move.from)) {
			LOG.info(Messages.text("stone_at_position_cannot_move", move.from));
		} else if (board.isEmptyPosition(move.to) && (player.canJump() || board.areNeighbors(move.from, move.to))) {
			return player.canJump() ? board.isEmptyPosition(move.to)
					: board.isEmptyPosition(move.to) && board.areNeighbors(move.from, move.to);
		}
		return false;
	}

	private Optional<Stone> stoneToMove() {
		return boardUI.stoneAt(move.from);
	}

	private Vector2 computeVelocity(Direction dir) {
		float speed = moveSpeed.apply(move.from, move.to);
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
		throw new IllegalArgumentException("Illegal direction: " + dir);
	}

	private boolean endPositionReached() {
		if (stoneToMove().isPresent()) {
			Stone stone = stoneToMove().get();
			float speed = stone.tf.getVelocity().length();
			Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
			Vector2 toCenter = boardUI.centerPoint(move.to);
			return stoneSpot.contains(new Point2D.Float(toCenter.x, toCenter.y));
		}
		return false;
	}
}