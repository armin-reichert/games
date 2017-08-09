package de.amr.games.muehle.play;

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
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.ui.Board;

/**
 * A player using mouse and keyboard for placing and moving stones.
 * 
 * @author Armin Reichert
 */
public class InteractivePlayer extends AbstractPlayer {

	private static final EnumMap<Direction, Integer> STEERING = new EnumMap<>(Direction.class);
	static {
		STEERING.put(NORTH, VK_UP);
		STEERING.put(EAST, VK_RIGHT);
		STEERING.put(SOUTH, VK_DOWN);
		STEERING.put(WEST, VK_LEFT);
	}

	public InteractivePlayer(MillApp app, Board board, StoneColor color) {
		super(app, board, color);
	}

	@Override
	public OptionalInt supplyPlacePosition() {
		return supplyMouseClickPosition();
	}

	@Override
	public OptionalInt supplyRemovalPosition(StoneColor opponentColor) {
		return supplyMouseClickPosition();
	}

	@Override
	public OptionalInt supplyMoveStartPosition() {
		return supplyMouseClickPosition();
	}

	@Override
	public OptionalInt supplyMoveEndPosition(int from) {
		// if end position is uniquely determined, use it
		if (!canJump() && model.emptyNeighbors(from).count() == 1) {
			return model.emptyNeighbors(from).findFirst();
		}
		// if move direction has been specified, use position in that direction
		Optional<Direction> optMoveDirection = supplyMoveDirection();
		if (optMoveDirection.isPresent()) {
			return model.neighbor(from, optMoveDirection.get());
		}
		// use mouse click position if possible
		return supplyMouseClickPosition();
	}

	private Optional<Direction> supplyMoveDirection() {
		/*@formatter:off*/
		return STEERING.entrySet().stream()
			.filter(e -> Keyboard.keyPressedOnce(e.getValue()))
			.map(Map.Entry::getKey)
			.findAny();
		/*@formatter:on*/
	}

	private OptionalInt supplyMouseClickPosition() {
		return Mouse.clicked() ? board.findPosition(Mouse.getX(), Mouse.getY()) : OptionalInt.empty();
	}
}