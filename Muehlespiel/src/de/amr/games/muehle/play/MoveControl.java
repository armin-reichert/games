package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.game.math.Vector2.dist;
import static de.amr.games.muehle.play.MoveState.FINISHED;
import static de.amr.games.muehle.play.MoveState.GOT_VALID_MOVE;
import static de.amr.games.muehle.play.MoveState.INITIAL;
import static de.amr.games.muehle.play.MoveState.JUMPING;
import static de.amr.games.muehle.play.MoveState.MOVING;
import static de.amr.games.muehle.play.MoveState.READING_MOVE;
import static java.lang.String.format;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;

import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.timing.Pulse;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.player.Player;
import de.amr.games.muehle.ui.BoardUI;
import de.amr.games.muehle.ui.Stone;

/**
 * A finite state machine for controlling the movement of stones.
 * 
 * @author Armin Reichert
 */
public class MoveControl extends StateMachine<MoveState, Object> {

	private static final float MOVE_TIME_SEC = .75f;

	private final BoardUI boardUI;
	private final Board board;
	private final Player player;
	private final Pulse pulse;

	private Move move;

	/**
	 * Constructs a move control.
	 * 
	 * @param boardUI
	 *          the board UI where the move is displayed
	 * @param player
	 *          the player who makes this move
	 * @param pulse
	 *          the pulse of the application
	 */
	public MoveControl(BoardUI boardUI, Player player, Pulse pulse) {
		super("Move Control", MoveState.class, READING_MOVE);

		this.boardUI = boardUI;
		this.board = boardUI.getModel();
		this.player = player;
		this.pulse = pulse;

		defineStateMachine();
		setLogger(LOG);

		init();
	}

	private void clear() {
		move = null;
		player.clearMove();
	}

	private void defineStateMachine() {

		// INITIAL

		state(INITIAL).entry = s -> clear();

		change(INITIAL, READING_MOVE);

		// READING_MOVE

		state(READING_MOVE).update = s -> move = player.supplyMove();

		change(READING_MOVE, INITIAL, () -> hasMoveStartPosition() && !isMoveStartPossible());

		change(READING_MOVE, INITIAL, () -> hasMovePositions() && !isMovePossible());

		change(READING_MOVE, GOT_VALID_MOVE, () -> hasMovePositions() && isMovePossible());

		// GOT_VALID_MOVE

		change(GOT_VALID_MOVE, MOVING, () -> board.areNeighbors(move.from, move.to));

		change(GOT_VALID_MOVE, JUMPING, player::canJump);

		// MOVING

		state(MOVING).entry = s -> {
			board.getDirection(move.from, move.to).ifPresent(moveDir -> {
				getMovedStone().ifPresent(stone -> stone.tf.setVelocity(computeMoveVelocity()));
				LOG.info(format("Moving stone from position %d to position %d towards %s", move.from, move.to, moveDir));
			});
		};

		state(MOVING).update = s -> getMovedStone().ifPresent(stone -> stone.tf.move());

		change(MOVING, FINISHED, () -> endPositionReached());

		state(MOVING).exit = s -> getMovedStone().ifPresent(stone -> stone.tf.setVelocity(0, 0));

		// JUMPING

		state(JUMPING).entry = s -> LOG.info(format("Jumping from %d to %d", move.from, move.to));

		change(JUMPING, FINISHED);

		// FINISHED

		state(FINISHED).entry = s -> boardUI.moveStone(move.from, move.to);
	}

	private Optional<Stone> getMovedStone() {
		return boardUI.getStoneAt(move.from);
	}

	public Optional<Move> getMove() {
		return move == null ? Optional.empty() : Optional.of(move);
	}

	public boolean hasMoveStartPosition() {
		return move != null && board.isValidPosition(move.from);
	}

	private boolean hasMovePositions() {
		return hasMoveStartPosition() && board.isValidPosition(move.to);
	}

	public boolean isMoveStartPossible() {
		return hasMoveStartPosition() && board.getStoneAt(move.from) == player.getColor()
				&& (player.canJump() || board.hasEmptyNeighbor(move.from));
	}

	private boolean isMovePossible() {
		if (!getMovedStone().isPresent()) {
			LOG.info(Messages.text("stone_at_position_not_existing", move.from));
		} else if (getMovedStone().get().getColor() != player.getColor()) {
			LOG.info(Messages.text("stone_at_position_wrong_color", move.from));
		} else if (!player.canJump() && !board.hasEmptyNeighbor(move.from)) {
			LOG.info(Messages.text("stone_at_position_cannot_move", move.from));
		} else if (board.isEmptyPosition(move.to) && (player.canJump() || board.areNeighbors(move.from, move.to))) {
			return player.canJump() ? board.isEmptyPosition(move.to)
					: board.isEmptyPosition(move.to) && board.areNeighbors(move.from, move.to);
		}
		return false;
	}

	private Vector2 computeMoveVelocity() {
		float speed = dist(boardUI.centerPoint(move.from), boardUI.centerPoint(move.to)) / pulse.secToTicks(MOVE_TIME_SEC);
		Direction dir = board.getDirection(move.from, move.to).get();
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

	private boolean endPositionReached() {
		if (getMovedStone().isPresent()) {
			Stone stone = getMovedStone().get();
			float speed = stone.tf.getVelocity().length();
			Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
			Vector2 toCenter = boardUI.centerPoint(move.to);
			return stoneSpot.contains(new Point2D.Float(toCenter.x, toCenter.y));
		}
		return false;
	}
}