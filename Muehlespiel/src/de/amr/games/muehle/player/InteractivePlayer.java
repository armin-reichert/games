package de.amr.games.muehle.player;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiFunction;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.msg.Messages;

/**
 * A player using mouse and keyboard for placing and moving stones.
 * 
 * @author Armin Reichert
 */
public class InteractivePlayer implements Player {

	private final EnumMap<Direction, Integer> steering = new EnumMap<>(Direction.class);
	private final Board board;
	private final StoneColor color;
	private final Move move;
	private BiFunction<Integer, Integer, OptionalInt> boardPositionFinder;

	public InteractivePlayer(Board board, StoneColor color) {
		this.steering.put(NORTH, VK_UP);
		this.steering.put(EAST, VK_RIGHT);
		this.steering.put(SOUTH, VK_DOWN);
		this.steering.put(WEST, VK_LEFT);
		this.board = board;
		this.color = color;
		this.move = new Move();
	}

	public void setBoardPositionFinder(BiFunction<Integer, Integer, OptionalInt> boardPositionFinder) {
		this.boardPositionFinder = boardPositionFinder;
	}

	@Override
	public String getName() {
		return Messages.text(color == WHITE ? "white" : "black");
	}

	@Override
	public boolean isInteractive() {
		return true;
	}

	@Override
	public Board getBoard() {
		return board;
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	@Override
	public OptionalInt supplyPlacingPosition() {
		return boardPositionClicked();
	}

	@Override
	public OptionalInt supplyRemovalPosition() {
		return boardPositionClicked();
	}

	@Override
	public void newMove() {
		move.from = move.to = -1;
	}

	@Override
	public Optional<Move> supplyMove() {
		if (move.from == -1) {
			boardPositionClicked().ifPresent(p -> {
				if (board.isEmptyPosition(p)) {
					LOG.info(Messages.text("stone_at_position_not_existing", p));
				} else if (!board.hasStoneAt(p, color)) {
					LOG.info(Messages.text("stone_at_position_wrong_color", p));
				} else if (!canJump() && !board.hasEmptyNeighbor(p)) {
					LOG.info(Messages.text("stone_at_position_cannot_move", p));
				} else {
					move.from = p;
					LOG.info("Move starts from " + p);
				}
			});
		} else if (move.to == -1) {
			supplyMoveEndPosition().ifPresent(p -> move.to = p);
			if (move.to != -1 && board.isEmptyPosition(move.to) && (canJump() || board.areNeighbors(move.from, move.to))) {
				LOG.info("Move leads to " + move.to);
				return Optional.of(move);
			} else {
				move.to = -1;
			}
		}
		return Optional.empty();
	}

	OptionalInt supplyMoveEndPosition() {
		// if end position is uniquely determined, use it
		if (!canJump() && board.emptyNeighbors(move.from).count() == 1) {
			return board.emptyNeighbors(move.from).findFirst();
		}
		// if move direction has been specified, use position in that direction
		Optional<Direction> optMoveDirection = supplyMoveDirection();
		if (optMoveDirection.isPresent()) {
			return board.neighbor(move.from, optMoveDirection.get());
		}
		// use mouse click position if possible
		return boardPositionClicked();
	}

	Optional<Direction> supplyMoveDirection() {
		/*@formatter:off*/
		return steering.entrySet().stream()
			.filter(e -> Keyboard.keyPressedOnce(e.getValue()))
			.map(Map.Entry::getKey)
			.findAny();
		/*@formatter:on*/
	}

	OptionalInt boardPositionClicked() {
		return Mouse.clicked() ? boardPositionFinder.apply(Mouse.getX(), Mouse.getY()) : OptionalInt.empty();
	}
}