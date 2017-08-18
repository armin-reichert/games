package de.amr.games.muehle.player.samples;

import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_UP;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;
import de.amr.games.muehle.ui.BoardUI;

/**
 * A player using mouse and keyboard for placing and moving stones.
 * 
 * @author Armin Reichert
 */
public class InteractivePlayer implements Player {

	private static final EnumMap<Direction, Integer> STEERING = new EnumMap<>(Direction.class);
	static {
		STEERING.put(NORTH, VK_UP);
		STEERING.put(EAST, VK_RIGHT);
		STEERING.put(SOUTH, VK_DOWN);
		STEERING.put(WEST, VK_LEFT);
	}

	private final Board board;
	private final StoneColor color;
	private BoardUI boardUI;
	private Move move;

	public InteractivePlayer(BoardUI boardUI, StoneColor color) {
		this.board = boardUI.getBoard();
		this.color = color;
		this.boardUI = boardUI;
		move = new Move();
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	@Override
	public OptionalInt supplyPlacingPosition() {
		return supplyMouseClickBoardPosition();
	}

	@Override
	public OptionalInt supplyRemovalPosition() {
		return supplyMouseClickBoardPosition();
	}

	@Override
	public Move supplyMove(boolean canJump) {
		if (move.from == -1) {
			supplyMouseClickBoardPosition().ifPresent(p -> move.from = p);
		} else if (move.to == -1) {
			supplyMoveEndPosition(canJump).ifPresent(p -> move.to = p);
		}
		return move;
	}

	@Override
	public void clearMove() {
		move = new Move();
	}

	private OptionalInt supplyMoveEndPosition(boolean canJump) {
		// if end position is uniquely determined, use it
		if (!canJump && board.emptyNeighbors(move.from).count() == 1) {
			return board.emptyNeighbors(move.from).findFirst();
		}
		// if move direction has been specified, use position in that direction
		Optional<Direction> optMoveDirection = supplyMoveDirection();
		if (optMoveDirection.isPresent()) {
			return board.neighbor(move.from, optMoveDirection.get());
		}
		// use mouse click position if possible
		return supplyMouseClickBoardPosition();
	}

	private Optional<Direction> supplyMoveDirection() {
		/*@formatter:off*/
		return STEERING.entrySet().stream()
			.filter(e -> Keyboard.keyPressedOnce(e.getValue()))
			.map(Map.Entry::getKey)
			.findAny();
		/*@formatter:on*/
	}

	private OptionalInt supplyMouseClickBoardPosition() {
		return Mouse.clicked() ? boardUI.findPosition(Mouse.getX(), Mouse.getY()) : OptionalInt.empty();
	}
}