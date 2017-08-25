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
import de.amr.easy.statemachine.State;
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
	private final Player player;

	private int from, to;

	/**
	 * Constructs a move control.
	 * 
	 * @param player
	 *          the player making the move
	 * @param boardUI
	 *          the board UI where the move is visualized
	 * @param moveSpeed
	 *          function computing the move speed between two board positions
	 */
	public MoveControl(Player player, BoardUI boardUI, BiFunction<Integer, Integer, Float> moveSpeed) {
		super("Move Control", MoveState.class, READING_MOVE);

		this.player = player;
		this.boardUI = boardUI;
		this.moveSpeed = moveSpeed;

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

		state(JUMPING).entry = s -> LOG.info(Messages.text("jumping_from_to", from, to));

		change(JUMPING, FINISHED);

		// FINISHED

		state(FINISHED).entry = s -> boardUI.moveStone(from, to);
	}

	void newMove(State state) {
		from = to = -1;
		player.newMove();
	}

	void readMove(State state) {
		Move move = player.supplyMove();
		if (move != null) {
			from = move.from;
			to = move.to;
		}
	}

	void startMove(State state) {
		board().getDirection(from, to).ifPresent(dir -> {
			stone().ifPresent(stone -> stone.tf.setVelocity(velocity(dir)));
			LOG.info(Messages.text("moving_from_to_towards", from, to, dir));
		});
	}

	void stopMove(State state) {
		stone().ifPresent(stone -> stone.tf.setVelocity(0, 0));
	}

	void moveStone(State state) {
		stone().ifPresent(stone -> stone.tf.move());
	}

	Board board() {
		return boardUI.board();
	}

	public Optional<Move> getMove() {
		return from != -1 && to != -1 ? Optional.of(new Move(from, to)) : Optional.empty();
	}

	public boolean hasMoveStartPosition() {
		return board().isValidPosition(from);
	}

	boolean hasBothMovePositions() {
		return hasMoveStartPosition() && board().isValidPosition(to);
	}

	public boolean isMoveStartPossible() {
		return hasMoveStartPosition() && board().hasStoneAt(from) && board().getStoneAt(from).get() == player.getColor()
				&& (player.canJump() || board().hasEmptyNeighbor(from));
	}

	boolean isMovePossible() {
		if (!stone().isPresent()) {
			LOG.info(Messages.text("stone_at_position_not_existing", from));
		} else if (stone().get().getColor() != player.getColor()) {
			LOG.info(Messages.text("stone_at_position_wrong_color", from));
		} else if (!player.canJump() && !board().hasEmptyNeighbor(from)) {
			LOG.info(Messages.text("stone_at_position_cannot_move", from));
		} else if (board().isEmptyPosition(to) && (player.canJump() || board().areNeighbors(from, to))) {
			return player.canJump() ? board().isEmptyPosition(to)
					: board().isEmptyPosition(to) && board().areNeighbors(from, to);
		}
		return false;
	}

	boolean isStoneAtTarget() {
		if (stone().isPresent()) {
			Stone stone = stone().get();
			float speed = stone.tf.getVelocity().length();
			Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
			Vector2 toCenter = boardUI.centerPoint(to);
			return stoneSpot.contains(new Point2D.Float(toCenter.x, toCenter.y));
		}
		return false;
	}

	Optional<Stone> stone() {
		return boardUI.stoneAt(from);
	}

	Vector2 velocity(Direction dir) {
		float speed = moveSpeed.apply(from, to);
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
}