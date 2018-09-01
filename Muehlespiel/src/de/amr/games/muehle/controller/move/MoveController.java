package de.amr.games.muehle.controller.move;

import static de.amr.games.muehle.controller.move.MoveEvent.GOT_MOVE_FROM_PLAYER;
import static de.amr.games.muehle.controller.move.MoveState.ANIMATION;
import static de.amr.games.muehle.controller.move.MoveState.COMPLETE;
import static de.amr.games.muehle.controller.move.MoveState.JUMPING;
import static de.amr.games.muehle.controller.move.MoveState.READING_MOVE;
import static de.amr.games.muehle.model.board.Board.areNeighbors;
import static de.amr.games.muehle.model.board.Board.getDirection;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Optional;

import de.amr.easy.game.Application;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.statemachine.State;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.controller.player.Player;
import de.amr.games.muehle.model.board.Direction;
import de.amr.games.muehle.model.board.Move;
import de.amr.games.muehle.msg.Messages;
import de.amr.games.muehle.view.MillGameUI;
import de.amr.games.muehle.view.Stone;

/**
 * A finite state machine for controlling the animated movement of stones.
 * 
 * @author Armin Reichert
 */
public class MoveController extends StateMachine<MoveState, MoveEvent> {

	private final Player player;
	private final MillGameUI gameUI;
	private final float moveTimeSec;
	private Move move;

	public MoveController(Player player, MillGameUI gameUI, float moveTimeSec) {
		super("Move Control", MoveState.class, READING_MOVE);

		this.player = player;
		this.gameUI = gameUI;
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

		state(JUMPING).entry = s -> Application.LOGGER.info(player.name() + ": "
				+ Messages.text("jumping_from_to", move.from().get(), move.to().get()));

		change(JUMPING, COMPLETE);

		// COMPLETE

		state(COMPLETE).entry = s -> gameUI.moveStone(move);

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
		if (move.isPresent()) {
			int from = move.from().get(), to = move.to().get();
			gameUI.getStoneAt(from).ifPresent(stone -> {
				float speed = Vector2f.dist(gameUI.getLocation(from), gameUI.getLocation(to))
						/ Application.CLOCK.secToTicks(moveTimeSec);
				Direction dir = getDirection(from, to).get();
				if (dir == Direction.NORTH) {
					stone.tf.setVelocity(0, -speed);
				} else if (dir == Direction.EAST) {
					stone.tf.setVelocity(speed, 0);
				} else if (dir == Direction.SOUTH) {
					stone.tf.setVelocity(0, speed);
				} else if (dir == Direction.WEST) {
					stone.tf.setVelocity(-speed, 0);
				}
				Application.LOGGER
						.info(player.name() + ": " + Messages.text("moving_from_to_towards", from, to, dir));
			});
		}
	}

	private void updateAnimation(State state) {
		move.from().ifPresent(from -> gameUI.getStoneAt(from).ifPresent(stone -> stone.tf.move()));
	}

	private void stopAnimation(State state) {
		move.from()
				.ifPresent(from -> gameUI.getStoneAt(from).ifPresent(stone -> stone.tf.setVelocity(0, 0)));
	}

	private boolean isMoveTargetReached() {
		if (move.isPresent()) {
			int from = move.from().get(), to = move.to().get();
			Optional<Stone> optStone = gameUI.getStoneAt(from);
			if (optStone.isPresent()) {
				Stone stone = optStone.get();
				float speed = stone.tf.getVelocity().length();
				Ellipse2D stoneSpot = new Ellipse2D.Float(stone.tf.getX() - speed, stone.tf.getY() - speed,
						2 * speed, 2 * speed);
				Vector2f toCenter = gameUI.getLocation(to);
				return stoneSpot.contains(new Point2D.Float(toCenter.x, toCenter.y));
			}
		}
		return false;
	}

	private boolean isMovePossible(Move move) {
		if (!move.isPresent()) {
			return false;
		}
		int from = move.from().get(), to = move.to().get();
		Optional<Stone> optStone = gameUI.getStoneAt(from);
		if (!optStone.isPresent()) {
			Application.LOGGER.info(Messages.text("stone_at_position_not_existing", from));
			return false;
		}
		Stone stone = optStone.get();
		if (stone.getColor() != player.color()) {
			Application.LOGGER.info(Messages.text("stone_at_position_wrong_color", from));
			return false;
		}
		if (!player.model().board.isEmptyPosition(to)) {
			Application.LOGGER.info(Messages.text("stone_at_position", to));
			return false;
		}
		if (player.canJump()) {
			return true;
		}
		if (!areNeighbors(from, to)) {
			Application.LOGGER.info(Messages.text("not_neighbors", from, to));
			return false;
		}
		return true;
	}
}