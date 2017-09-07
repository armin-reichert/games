package de.amr.games.muehle.gameplay;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.game.math.Vector2f.dist;
import static de.amr.games.muehle.gameplay.MoveEvent.GOT_MOVE_FROM_PLAYER;
import static de.amr.games.muehle.gameplay.MoveState.ANIMATION;
import static de.amr.games.muehle.gameplay.MoveState.COMPLETE;
import static de.amr.games.muehle.gameplay.MoveState.JUMPING;
import static de.amr.games.muehle.gameplay.MoveState.READING_MOVE;

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
import de.amr.games.muehle.player.Move;
import de.amr.games.muehle.player.Player;
import de.amr.games.muehle.ui.Stone;

/**
 * A finite state machine for controlling the animated movement of stones.
 * 
 * @author Armin Reichert
 */
public class MoveControl extends StateMachine<MoveState, MoveEvent> {

	private final Board board;
	private final Player player;
	private final MillGameUI gameUI;
	private final Pulse pulse;
	private final float moveTimeSec;
	private Move move;

	public MoveControl(Player player, MillGameUI gameUI, Pulse pulse, float moveTimeSec) {
		super("Move Control", MoveState.class, READING_MOVE);

		this.player = player;
		this.board = player.getBoard();
		this.gameUI = gameUI;
		this.pulse = pulse;
		this.moveTimeSec = moveTimeSec;
		this.move = null;

		// READING_MOVE

		state(READING_MOVE).entry = this::newMove;

		state(READING_MOVE).update = this::requestMoveFromPlayer;

		changeOnInput(GOT_MOVE_FROM_PLAYER, READING_MOVE, JUMPING, player::canJump);

		changeOnInput(GOT_MOVE_FROM_PLAYER, READING_MOVE, ANIMATION);

		// MOVING

		state(ANIMATION).entry = this::startAnimation;

		state(ANIMATION).update = this::updateAnimation;

		change(ANIMATION, COMPLETE, this::isMoveTargetReached);

		state(ANIMATION).exit = this::stopAnimation;

		// JUMPING

		state(JUMPING).entry = s -> LOG
				.info(player.getName() + ": " + Messages.text("jumping_from_to", move.from, move.to));

		change(JUMPING, COMPLETE);

		// COMPLETE

		state(COMPLETE).entry = s -> gameUI.moveStone(move.from, move.to);

	}

	public Optional<Move> getMove() {
		return Optional.ofNullable(move);
	}

	private void newMove(State state) {
		move = null;
		player.newMove();
	}

	private void requestMoveFromPlayer(State state) {
		player.supplyMove().ifPresent(move -> {
			if (isMovePossible(move)) {
				this.move = move;
				addInput(GOT_MOVE_FROM_PLAYER);
			}
		});
	}

	private void startAnimation(State state) {
		gameUI.getStoneAt(move.from).ifPresent(stone -> {
			float speed = dist(gameUI.getLocation(move.from), gameUI.getLocation(move.to)) / pulse.secToTicks(moveTimeSec);
			Direction dir = board.getDirection(move.from, move.to).get();
			if (dir == Direction.NORTH) {
				stone.tf.setVelocity(0, -speed);
			} else if (dir == Direction.EAST) {
				stone.tf.setVelocity(speed, 0);
			} else if (dir == Direction.SOUTH) {
				stone.tf.setVelocity(0, speed);
			} else if (dir == Direction.WEST) {
				stone.tf.setVelocity(-speed, 0);
			}
			LOG.info(player.getName() + ": " + Messages.text("moving_from_to_towards", move.from, move.to, dir));
		});
	}

	private void updateAnimation(State state) {
		gameUI.getStoneAt(move.from).ifPresent(stone -> stone.tf.move());
	}

	private void stopAnimation(State state) {
		gameUI.getStoneAt(move.from).ifPresent(stone -> stone.tf.setVelocity(0, 0));
	}

	private boolean isMoveTargetReached() {
		Optional<Stone> optStone = gameUI.getStoneAt(move.from);
		if (optStone.isPresent()) {
			Stone stone = optStone.get();
			float speed = stone.tf.getVelocity().length();
			Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed, 2 * speed, 2 * speed);
			Vector2f toCenter = gameUI.getLocation(move.to);
			return stoneSpot.contains(new Point2D.Float(toCenter.x, toCenter.y));
		}
		return false;
	}

	private boolean isMovePossible(Move move) {
		Optional<Stone> optStone = gameUI.getStoneAt(move.from);
		if (!optStone.isPresent()) {
			LOG.info(Messages.text("stone_at_position_not_existing", move.from));
			return false;
		}
		Stone stone = optStone.get();
		if (stone.getColor() != player.getColor()) {
			LOG.info(Messages.text("stone_at_position_wrong_color", move.from));
			return false;
		}
		if (!board.isEmptyPosition(move.to)) {
			LOG.info(Messages.text("stone_at_position", move.to));
			return false;
		}
		if (player.canJump()) {
			return true;
		}
		if (!board.areNeighbors(move.from, move.to)) {
			LOG.info(Messages.text("not_neighbors", move.from, move.to));
			return false;
		}
		return true;
	}
}