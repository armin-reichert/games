package de.amr.games.muehle.play;

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
public class InteractivePlayer implements Player {

	private static final EnumMap<Direction, Integer> DIRECTION_KEYS = new EnumMap<>(Direction.class);
	static {
		DIRECTION_KEYS.put(NORTH, VK_UP);
		DIRECTION_KEYS.put(EAST, VK_RIGHT);
		DIRECTION_KEYS.put(SOUTH, VK_DOWN);
		DIRECTION_KEYS.put(WEST, VK_LEFT);
	}

	private final MillApp app;
	private final Board board;
	private final StoneColor color;
	private int stonesPlaced;

	public InteractivePlayer(MillApp app, Board board, StoneColor color) {
		this.app = app;
		this.board = board;
		this.color = color;
	}

	@Override
	public void init() {
		stonesPlaced = 0;
	}

	@Override
	public int getStonesPlaced() {
		return stonesPlaced;
	}

	@Override
	public StoneColor getColor() {
		return color;
	}

	@Override
	public boolean canJump() {
		return board.getModel().stoneCount(color) == 3;
	}

	@Override
	public OptionalInt tryToPlaceStone() {
		OptionalInt optClickPosition = findMouseClickPosition();
		if (optClickPosition.isPresent()) {
			int clickPosition = optClickPosition.getAsInt();
			if (board.getModel().hasStoneAt(clickPosition)) {
				LOG.info(app.msg("stone_at_position", clickPosition));
			} else {
				board.putStoneAt(clickPosition, color);
				stonesPlaced += 1;
				return optClickPosition;
			}
		}
		return OptionalInt.empty();
	}

	@Override
	public OptionalInt tryToRemoveStone(StoneColor opponentColor) {
		OptionalInt optClickPosition = findMouseClickPosition();
		if (optClickPosition.isPresent()) {
			int clickPosition = optClickPosition.getAsInt();
			if (board.getModel().isEmpty(clickPosition)) {
				LOG.info(app.msg("stone_at_position_not_existing", clickPosition));
			} else if (board.getModel().getStoneAt(clickPosition) != opponentColor) {
				LOG.info(app.msg("stone_at_position_wrong_color", clickPosition));
			} else if (board.getModel().isPositionInsideMill(clickPosition, opponentColor)
					&& !board.getModel().areAllStonesInsideMill(opponentColor)) {
				LOG.info(app.msg("stone_cannot_be_removed_from_mill"));
			} else {
				board.removeStoneAt(clickPosition);
				LOG.info(app.msg(color == WHITE ? "white_took_stone" : "black_took_stone"));
				return optClickPosition;
			}
		}
		return OptionalInt.empty();
	}

	@Override
	public OptionalInt supplyMoveStart() {
		if (Mouse.clicked()) {
			OptionalInt optStartPosition = board.findPosition(Mouse.getX(), Mouse.getY());
			if (optStartPosition.isPresent()) {
				int from = optStartPosition.getAsInt();
				Optional<Stone> optStone = board.getStoneAt(from);
				if (!optStone.isPresent()) {
					LOG.info(app.msg("stone_at_position_not_existing", from));
				} else if (color != optStone.get().getColor()) {
					LOG.info(app.msg("stone_at_position_wrong_color", from));
				} else if (!canJump() && !board.getModel().hasEmptyNeighbor(from)) {
					LOG.info(app.msg("stone_at_position_cannot_move", from));
				} else {
					return optStartPosition;
				}
			}
		}
		return OptionalInt.empty();
	}

	@Override
	public OptionalInt supplyMoveEnd(int from) {
		// if target position is unique, use it
		if (!canJump() && board.getModel().emptyNeighbors(from).count() == 1) {
			return board.getModel().emptyNeighbors(from).findFirst();
		}
		// if move direction was specified and board position in that direction is empty, use it
		Optional<Direction> optMoveDirection = supplyMoveDirection();
		if (optMoveDirection.isPresent()) {
			Direction dir = optMoveDirection.get();
			OptionalInt optNeighbor = board.getModel().neighbor(from, dir);
			if (optNeighbor.isPresent()) {
				int neighbor = optNeighbor.getAsInt();
				if (board.getModel().isEmpty(neighbor)) {
					return OptionalInt.of(neighbor);
				}
			}
		}
		// if target position was selected with mouse click and move to that position is possible, use it
		if (Mouse.clicked()) {
			OptionalInt optClickPos = board.findPosition(Mouse.getX(), Mouse.getY());
			if (optClickPos.isPresent()) {
				int clickPos = optClickPos.getAsInt();
				if (!board.getModel().hasStoneAt(clickPos) && (canJump() || board.getModel().areNeighbors(from, clickPos))) {
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

	private Optional<Direction> supplyMoveDirection() {
		/*@formatter:off*/
		return DIRECTION_KEYS.entrySet().stream()
			.filter(e -> Keyboard.keyPressedOnce(e.getValue()))
			.map(Map.Entry::getKey)
			.findAny();
		/*@formatter:on*/
	}
}