package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.play.Move.MoveState.COMPLETE;
import static de.amr.games.muehle.play.Move.MoveState.INITIAL;
import static de.amr.games.muehle.play.Move.MoveState.JUMPING;
import static de.amr.games.muehle.play.Move.MoveState.KNOWS_FROM;
import static de.amr.games.muehle.play.Move.MoveState.KNOWS_TO;
import static de.amr.games.muehle.play.Move.MoveState.MOVING;
import static java.lang.String.format;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import de.amr.easy.game.math.Vector2;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.ui.Board;
import de.amr.games.muehle.ui.Stone;

/**
 * Move of a stone on the board. Controls the different phases of a move like getting start and target position,
 * determining if moving or jumping and animating the move. The move life-cycle is controlled by a finite state machine.
 * 
 * @author Armin Reichert
 */
public class Move {

	private final Player player;
	private final Board board;
	private final Supplier<Vector2> velocitySupplier;
	private final MoveControl control;

	private OptionalInt optFrom;
	private OptionalInt optTo;
	private Point2D toPoint;
	private Stone stone;

	/**
	 * Constructs an initial move.
	 * 
	 * @player the player who makes this move
	 * @param board
	 *          the board where the move is executed
	 * @param velocitySupplier
	 *          supplies the move velocity
	 */
	public Move(Board board, Player player, Supplier<Vector2> velocitySupplier) {
		this.player = player;
		this.board = board;
		this.velocitySupplier = velocitySupplier;
		control = new MoveControl();
		// control.setLogger(LOG);
		init();
	}

	public enum MoveState {
		INITIAL, KNOWS_FROM, KNOWS_TO, MOVING, JUMPING, COMPLETE;
	};

	/** Finite state machine controlling the move */
	private class MoveControl extends StateMachine<MoveState, Object> {

		public MoveControl() {
			super("Move Control", MoveState.class, INITIAL);

			state(INITIAL).entry = s -> clear();

			state(INITIAL).update = s -> optFrom = player.supplyMoveStartPosition();

			change(INITIAL, KNOWS_FROM, () -> gotValidMoveStart());

			state(KNOWS_FROM).update = s -> optTo = player.supplyMoveEndPosition(optFrom.getAsInt());

			change(KNOWS_FROM, KNOWS_TO, () -> gotValidMoveEnd());

			change(KNOWS_TO, MOVING, () -> board.getModel().areNeighbors(optFrom.getAsInt(), optTo.getAsInt()));

			change(KNOWS_TO, JUMPING, player::canJump);

			state(MOVING).entry = s -> {
				Direction dir = board.getModel().getDirection(optFrom.getAsInt(), optTo.getAsInt()).get();
				stone = board.getStoneAt(optFrom.getAsInt()).get();
				stone.tf.setVelocity(velocitySupplier.get());
				Vector2 toVector = board.centerPoint(optTo.getAsInt());
				toPoint = new Point2D.Float(toVector.x, toVector.y);
				LOG.info(format("Moving from %d to %d towards %s", optFrom.getAsInt(), optTo.getAsInt(), dir));
			};

			state(MOVING).update = s -> stone.tf.move();

			change(MOVING, COMPLETE, () -> isEndPositionReached());

			state(MOVING).exit = s -> stone.tf.setVelocity(0, 0);

			state(JUMPING).entry = s -> {
				stone = board.getStoneAt(optFrom.getAsInt()).get();
				LOG.info(format("Jumping from %d to %d", optFrom.getAsInt(), optTo.getAsInt()));
			};

			change(JUMPING, COMPLETE);

			state(COMPLETE).entry = s -> board.moveStone(optFrom.getAsInt(), optTo.getAsInt());
		}
	}

	public void init() {
		control.init();
	}

	public void update() {
		control.update();
	}

	public boolean isComplete() {
		return control.is(COMPLETE);
	}

	public OptionalInt getFrom() {
		return optFrom;
	}

	public OptionalInt getTo() {
		return optTo;
	}

	private boolean gotValidMoveStart() {
		if (!optFrom.isPresent()) {
			return false;
		}
		int from = optFrom.getAsInt();
		Optional<Stone> optStone = board.getStoneAt(from);
		if (!optStone.isPresent()) {
			LOG.info(Messages.text("stone_at_position_not_existing", from));
		} else if (optStone.get().getColor() != player.getColor()) {
			LOG.info(Messages.text("stone_at_position_wrong_color", from));
		} else if (!player.canJump() && !board.getModel().hasEmptyNeighbor(from)) {
			LOG.info(Messages.text("stone_at_position_cannot_move", from));
		} else {
			return true;
		}
		return false;
	}

	private boolean gotValidMoveEnd() {
		return optTo.isPresent() && board.getModel().isEmptyPosition(optTo.getAsInt())
				&& (player.canJump() || board.getModel().areNeighbors(optFrom.getAsInt(), optTo.getAsInt()));
	}

	private void clear() {
		optFrom = OptionalInt.empty();
		optTo = OptionalInt.empty();
		toPoint = null;
		stone = null;
	}

	private boolean isEndPositionReached() {
		float speed = stone.tf.getVelocity().length();
		Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
		return stoneSpot.contains(toPoint);
	}
}