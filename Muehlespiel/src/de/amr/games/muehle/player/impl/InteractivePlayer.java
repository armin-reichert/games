package de.amr.games.muehle.player.impl;

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
import java.util.function.BiFunction;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.player.api.Player;

/**
 * A player using mouse and keyboard for placing and moving stones.
 * 
 * @author Armin Reichert
 */
public class InteractivePlayer implements Player {

	static final EnumMap<Direction, Integer> STEERING = new EnumMap<>(Direction.class);

	static {
		STEERING.put(NORTH, VK_UP);
		STEERING.put(EAST, VK_RIGHT);
		STEERING.put(SOUTH, VK_DOWN);
		STEERING.put(WEST, VK_LEFT);
	}

	final Board board;
	final StoneColor color;
	final BiFunction<Integer, Integer, OptionalInt> boardPositionFinder;
	Move move;

	public InteractivePlayer(Board board, StoneColor color,
			BiFunction<Integer, Integer, OptionalInt> boardPositionFinder) {
		this.board = board;
		this.color = color;
		this.boardPositionFinder = boardPositionFinder;
		move = new Move();
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
	public boolean canJump() {
		return board.stoneCount(color) == 3;
	}

	@Override
	public OptionalInt supplyPlacingPosition() {
		return findClickedBoardPosition();
	}

	@Override
	public OptionalInt supplyRemovalPosition() {
		return findClickedBoardPosition();
	}

	@Override
	public Move supplyMove() {
		if (move.from == -1) {
			findClickedBoardPosition().ifPresent(p -> move.from = p);
		} else if (move.to == -1) {
			supplyMoveEndPosition().ifPresent(p -> move.to = p);
		}
		return move;
	}

	@Override
	public void newMove() {
		move = new Move();
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
		return findClickedBoardPosition();
	}

	Optional<Direction> supplyMoveDirection() {
		/*@formatter:off*/
		return STEERING.entrySet().stream()
			.filter(e -> Keyboard.keyPressedOnce(e.getValue()))
			.map(Map.Entry::getKey)
			.findAny();
		/*@formatter:on*/
	}

	OptionalInt findClickedBoardPosition() {
		return Mouse.clicked() ? boardPositionFinder.apply(Mouse.getX(), Mouse.getY()) : OptionalInt.empty();
	}
}