package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
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
import de.amr.games.muehle.ui.Stone;

/**
 * A player using mouse and keyboard for placing and moving stones.
 * 
 * @author Armin Reichert
 */
public class InteractivePlayer extends AbstractPlayer {

	private static final EnumMap<Direction, Integer> DIRECTION_KEYS = new EnumMap<>(Direction.class);
	static {
		DIRECTION_KEYS.put(NORTH, VK_UP);
		DIRECTION_KEYS.put(EAST, VK_RIGHT);
		DIRECTION_KEYS.put(SOUTH, VK_DOWN);
		DIRECTION_KEYS.put(WEST, VK_LEFT);
	}

	private static Optional<Direction> supplyMoveDirection() {
		/*@formatter:off*/
		return DIRECTION_KEYS.entrySet().stream()
			.filter(e -> Keyboard.keyPressedOnce(e.getValue()))
			.map(Map.Entry::getKey)
			.findAny();
		/*@formatter:on*/
	}

	public InteractivePlayer(MillApp app, Board board, StoneColor color) {
		super(app, board, color);
	}

	@Override
	public OptionalInt supplyPlacePosition() {
		return findMouseClickPosition();
	}

	@Override
	public OptionalInt supplyRemovePosition(StoneColor opponentColor) {
		return findMouseClickPosition();
	}

	@Override
	public OptionalInt supplyMoveStartPosition() {
		if (Mouse.clicked()) {
			OptionalInt optStartPosition = board.findPosition(Mouse.getX(), Mouse.getY());
			if (optStartPosition.isPresent()) {
				int from = optStartPosition.getAsInt();
				Optional<Stone> optStone = board.getStoneAt(from);
				if (!optStone.isPresent()) {
					LOG.info(app.msg("stone_at_position_not_existing", from));
				} else if (optStone.get().getColor() != color) {
					LOG.info(app.msg("stone_at_position_wrong_color", from));
				} else if (!canJump() && !model.hasEmptyNeighbor(from)) {
					LOG.info(app.msg("stone_at_position_cannot_move", from));
				} else {
					return optStartPosition;
				}
			}
		}
		return OptionalInt.empty();
	}

	@Override
	public OptionalInt supplyMoveEndPosition(int from) {
		// if target position is unique, use it
		if (!canJump() && model.emptyNeighbors(from).count() == 1) {
			return model.emptyNeighbors(from).findFirst();
		}
		// if move direction was specified and board position in that direction is empty, use it
		Optional<Direction> optMoveDirection = supplyMoveDirection();
		if (optMoveDirection.isPresent()) {
			Direction dir = optMoveDirection.get();
			OptionalInt optNeighbor = model.neighbor(from, dir);
			if (optNeighbor.isPresent()) {
				int neighbor = optNeighbor.getAsInt();
				if (model.isEmptyPosition(neighbor)) {
					return optNeighbor;
				}
			}
		}
		// if target position was selected with mouse click and move to that position is possible, use it
		if (Mouse.clicked()) {
			OptionalInt optClickPos = board.findPosition(Mouse.getX(), Mouse.getY());
			if (optClickPos.isPresent()) {
				int clickPos = optClickPos.getAsInt();
				if (model.isEmptyPosition(clickPos) && (canJump() || model.areNeighbors(from, clickPos))) {
					return optClickPos;
				}
			}
		}
		// no move end position could be determined
		return OptionalInt.empty();
	}

	private OptionalInt findMouseClickPosition() {
		return Mouse.clicked() ? board.findPosition(Mouse.getX(), Mouse.getY()) : OptionalInt.empty();
	}
}