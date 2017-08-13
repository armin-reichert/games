package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.game.math.Vector2.dist;
import static de.amr.games.muehle.play.MoveControl.MoveState.COMPLETE;
import static de.amr.games.muehle.play.MoveControl.MoveState.GOT_VALID_MOVE;
import static de.amr.games.muehle.play.MoveControl.MoveState.JUMPING;
import static de.amr.games.muehle.play.MoveControl.MoveState.MOVING;
import static de.amr.games.muehle.play.MoveControl.MoveState.READING_MOVE;
import static java.lang.String.format;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;

import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.timing.Pulse;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.ui.Board;
import de.amr.games.muehle.ui.Stone;

/**
 * Move of a stone on the board. Controls the different phases of a move like getting start and target position,
 * determining if moving or jumping and animating the move. The move life-cycle is controlled by a finite state machine.
 * 
 * @author Armin Reichert
 */
public class MoveControl {

	private static final float MOVE_SECONDS = .75f;

	private final Board board;
	private final Player player;
	private final Pulse pulse;
	private final FSM control;

	private Move move;
	private Point2D toPoint;
	private Stone stone;

	/**
	 * Constructs an initial move.
	 * 
	 * @param player
	 *          the player who makes this move
	 * @param board
	 *          the board where the move is executed
	 * @param pulse
	 *          the pulse of the application
	 */
	public MoveControl(Board board, Player player, Pulse pulse) {
		this.board = board;
		this.player = player;
		this.pulse = pulse;
		control = new FSM();
		init();
	}

	public enum MoveState {
		READING_MOVE, GOT_VALID_MOVE, MOVING, JUMPING, COMPLETE;
	};

	/** Finite state machine controlling the move */
	private class FSM extends StateMachine<MoveState, Object> {

		public FSM() {
			super("Move Control", MoveState.class, READING_MOVE);

			state(READING_MOVE).entry = s -> clear();

			state(READING_MOVE).update = s -> move = player.supplyMove();

			change(READING_MOVE, READING_MOVE, () -> hasMoveStart() && !isMoveStartPossible(), (s, t) -> clear());

			change(READING_MOVE, GOT_VALID_MOVE, () -> isMoveComplete() && isMovePossible());

			change(READING_MOVE, READING_MOVE, () -> isMoveComplete() && !isMovePossible(), (s, t) -> clear());

			change(GOT_VALID_MOVE, MOVING, () -> board.getModel().areNeighbors(move.from, move.to));

			change(GOT_VALID_MOVE, JUMPING, player::canJump);

			state(MOVING).entry = s -> {
				board.getModel().getDirection(move.from, move.to).ifPresent(dir -> {
					stone = board.getStoneAt(move.from).get();
					stone.tf.setVelocity(supplyMoveVelocity());
					Vector2 toVector = board.centerPoint(move.to);
					toPoint = new Point2D.Float(toVector.x, toVector.y);
					LOG.info(format("Moving from %d to %d towards %s", move.from, move.to, dir));
				});
			};

			state(MOVING).update = s -> stone.tf.move();

			change(MOVING, COMPLETE, () -> isEndPositionReached());

			state(MOVING).exit = s -> stone.tf.setVelocity(0, 0);

			state(JUMPING).entry = s -> {
				stone = board.getStoneAt(move.from).get();
				LOG.info(format("Jumping from %d to %d", move.from, move.to));
			};

			change(JUMPING, COMPLETE);

			state(COMPLETE).entry = s -> board.moveStone(move.from, move.to);
		}
	}

	public void init() {
		control.setLogger(LOG);
		control.init();
	}

	public void update() {
		control.update();
	}

	public boolean isAnimationComplete() {
		return control.is(COMPLETE);
	}

	public Move getMove() {
		return move;
	}

	public boolean hasMoveStart() {
		return move != null && board.getModel().isValidPosition(move.from);
	}

	public boolean isMoveStartPossible() {
		return move != null && move.from != -1 && board.getModel().getStoneAt(move.from) == player.getColor()
				&& (player.canJump() || board.getModel().hasEmptyNeighbor(move.from));
	}

	private boolean isMoveComplete() {
		return move != null && board.getModel().isValidPosition(move.from) && board.getModel().isValidPosition(move.to);
	}

	private boolean isMovePossible() {
		Optional<Stone> optStone = board.getStoneAt(move.from);
		if (!optStone.isPresent()) {
			LOG.info(Messages.text("stone_at_position_not_existing", move.from));
		} else if (optStone.get().getColor() != player.getColor()) {
			LOG.info(Messages.text("stone_at_position_wrong_color", move.from));
		} else if (!player.canJump() && !board.getModel().hasEmptyNeighbor(move.from)) {
			LOG.info(Messages.text("stone_at_position_cannot_move", move.from));
		} else if (board.getModel().isEmptyPosition(move.to)
				&& (player.canJump() || board.getModel().areNeighbors(move.from, move.to))) {
			return player.canJump() ? board.getModel().isEmptyPosition(move.to)
					: board.getModel().isEmptyPosition(move.to) && board.getModel().areNeighbors(move.from, move.to);
		}
		return false;
	}

	private void clear() {
		move = null;
		toPoint = null;
		stone = null;
		player.clearMove();
	}

	private Vector2 supplyMoveVelocity() {
		float speed = dist(board.centerPoint(move.from), board.centerPoint(move.to)) / pulse.secToTicks(MOVE_SECONDS);
		Direction dir = board.getModel().getDirection(move.from, move.to).get();
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

	private boolean isEndPositionReached() {
		float speed = stone.tf.getVelocity().length();
		Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
		return stoneSpot.contains(toPoint);
	}
}