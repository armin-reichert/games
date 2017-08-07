package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.game.math.Vector2.dist;
import static de.amr.games.muehle.MillApp.messages;
import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static de.amr.games.muehle.board.StoneType.BLACK;
import static de.amr.games.muehle.board.StoneType.WHITE;
import static de.amr.games.muehle.play.GamePhase.GAME_OVER;
import static de.amr.games.muehle.play.GamePhase.MOVING;
import static de.amr.games.muehle.play.GamePhase.PLACING;
import static de.amr.games.muehle.play.GamePhase.STARTED;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.input.Mouse;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.BoardModel;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.StoneType;
import de.amr.games.muehle.ui.Board;
import de.amr.games.muehle.ui.Move;
import de.amr.games.muehle.ui.Stone;
import de.amr.games.muehle.ui.StonesCounter;

/**
 * The play scene of the game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	private static final int NUM_STONES = 9;

	private static final EnumMap<Direction, Integer> DIRECTION_KEYS = new EnumMap<>(Direction.class);
	static {
		DIRECTION_KEYS.put(NORTH, KeyEvent.VK_UP);
		DIRECTION_KEYS.put(EAST, KeyEvent.VK_RIGHT);
		DIRECTION_KEYS.put(SOUTH, KeyEvent.VK_DOWN);
		DIRECTION_KEYS.put(WEST, KeyEvent.VK_LEFT);
	}

	private final PlayControl control;

	private BoardModel boardModel;
	private Board board;
	private StonesCounter whiteStonesToPlaceCounter;
	private StonesCounter blackStonesToPlaceCounter;
	private ScrollingText messageDisplay;
	private Move move;
	private StoneType turn;
	private StoneType winner;
	private int whiteStonesPlaced;
	private int blackStonesPlaced;
	private boolean assistantOn;

	/* A finite-state machine which controls the play scene */
	private class PlayControl extends StateMachine<GamePhase, String> {

		private boolean mustRemoveStoneOfOpponent;

		public PlayControl() {
			super("Mühlespiel-Steuerung", GamePhase.class, STARTED);

			// STARTED

			state(STARTED).entry = s -> displayMessage("newgame");

			change(STARTED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(PLACING).entry = s -> {
				board.clear();
				whiteStonesPlaced = 0;
				blackStonesPlaced = 0;
				mustRemoveStoneOfOpponent = false;
				turnPlacingTo(WHITE);
			};

			state(PLACING).update = s -> {
				if (mustRemoveStoneOfOpponent) {
					tryToRemoveStone(opponent()).ifPresent(pos -> {
						mustRemoveStoneOfOpponent = false;
						turnPlacingTo(opponent());
					});
				} else {
					tryToPlaceStone().ifPresent(pos -> {
						if (boardModel.isPositionInsideMill(pos, turn)) {
							mustRemoveStoneOfOpponent = true;
							displayMessage(isWhitesTurn() ? "white_must_take" : "black_must_take");
						} else {
							turnPlacingTo(opponent());
						}
					});
				}
			};

			change(PLACING, MOVING, () -> blackStonesPlaced == NUM_STONES && !mustRemoveStoneOfOpponent);

			// MOVING

			state(MOVING).entry = s -> turnMovingTo(turn);

			state(MOVING).update = s -> {
				if (mustRemoveStoneOfOpponent) {
					tryToRemoveStone(opponent()).ifPresent(pos -> {
						mustRemoveStoneOfOpponent = false;
						turnMovingTo(opponent());
					});
				} else {
					move.update();
					if (move.isComplete()) {
						if (boardModel.isPositionInsideMill(move.getTo().getAsInt(), turn)) {
							mustRemoveStoneOfOpponent = true;
							displayMessage(isWhitesTurn() ? "white_must_take" : "black_must_take");
						} else {
							turnMovingTo(opponent());
						}
						move.init();
					}
				}
			};

			change(MOVING, GAME_OVER, PlayScene.this::isGameOver);

			// GAME_OVER

			state(GAME_OVER).entry = s -> setWinner(opponent());

			change(GAME_OVER, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_ENTER));
		}
	}

	public PlayScene(MillApp app) {
		super(app);
		control = new PlayControl();
	}

	@Override
	public void init() {
		Font msgFont = Assets.storeFont("message-font", "fonts/Cookie-Regular.ttf", 40, Font.PLAIN);

		boardModel = new BoardModel();

		board = new Board(boardModel, 600, 600);
		board.hCenter(getWidth());
		board.tf.setY(50);

		move = new Move(board, this::supplyMoveStart, this::supplyMoveEnd, this::supplyMoveVelocity, this::canJump);

		whiteStonesToPlaceCounter = new StonesCounter(WHITE, () -> NUM_STONES - whiteStonesPlaced);
		whiteStonesToPlaceCounter.tf.moveTo(40, getHeight() - 50);
		whiteStonesToPlaceCounter.init();

		blackStonesToPlaceCounter = new StonesCounter(BLACK, () -> NUM_STONES - blackStonesPlaced);
		blackStonesToPlaceCounter.tf.moveTo(getWidth() - 100, getHeight() - 50);
		blackStonesToPlaceCounter.init();

		messageDisplay = new ScrollingText();
		messageDisplay.setColor(Color.BLUE);
		messageDisplay.setFont(msgFont);
		messageDisplay.tf.moveTo(0, getHeight() - 90);

		control.setLogger(LOG);
		control.init();
	}

	@Override
	public void update() {
		readInput();
		control.update();
	}

	private void readInput() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			control.init();
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_A)) {
			assistantOn = !assistantOn;
			LOG.info(msg(assistantOn ? "assistant_on" : "assistant_off"));
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_N)) {
			board.togglePositionNumbers();
		}
	}

	private String msg(String key, Object... args) {
		return MessageFormat.format(messages.getString(key), args);
	}

	private void displayMessage(String key, Object... args) {
		messageDisplay.setText(msg(key, args));
	}

	private boolean isWhitesTurn() {
		return turn == WHITE;
	}

	private StoneType opponent() {
		return turn == WHITE ? BLACK : WHITE;
	}

	private void turnPlacingTo(StoneType type) {
		turn = type;
		whiteStonesToPlaceCounter.setSelected(isWhitesTurn());
		blackStonesToPlaceCounter.setSelected(!isWhitesTurn());
		displayMessage(isWhitesTurn() ? "white_must_place" : "black_must_place");
	}

	private void turnMovingTo(StoneType type) {
		turn = type;
		displayMessage(isWhitesTurn() ? "white_must_move" : "black_must_move");
	}

	private void setWinner(StoneType type) {
		winner = type;
		displayMessage(winner == WHITE ? "white_wins" : "black_wins");
	}

	private boolean isGameOver() {
		return boardModel.stoneCount(turn) < 3 || (!canJump() && boardModel.isTrapped(turn));
	}

	private boolean canJump() {
		return boardModel.stoneCount(turn) == 3;
	}

	// Placing stones

	private OptionalInt findMouseClickPosition() {
		return Mouse.clicked() ? board.findPosition(Mouse.getX(), Mouse.getY()) : OptionalInt.empty();
	}

	private OptionalInt tryToPlaceStone() {
		OptionalInt optClickPosition = findMouseClickPosition();
		if (optClickPosition.isPresent()) {
			int clickPosition = optClickPosition.getAsInt();
			if (boardModel.hasStoneAt(clickPosition)) {
				LOG.info(msg("stone_at_position", clickPosition));
			} else {
				if (isWhitesTurn()) {
					board.putStoneAt(clickPosition, WHITE);
					whiteStonesPlaced += 1;
				} else {
					board.putStoneAt(clickPosition, BLACK);
					blackStonesPlaced += 1;
				}
				return optClickPosition;
			}
		}
		return OptionalInt.empty();
	}

	private OptionalInt tryToRemoveStone(StoneType color) {
		OptionalInt optClickPosition = findMouseClickPosition();
		if (optClickPosition.isPresent()) {
			int clickPosition = optClickPosition.getAsInt();
			if (boardModel.isEmpty(clickPosition)) {
				LOG.info(msg("stone_at_position_not_existing", clickPosition));
			} else if (boardModel.getStoneAt(clickPosition) != color) {
				LOG.info(msg("stone_at_position_wrong_color", clickPosition));
			} else if (boardModel.isPositionInsideMill(clickPosition, color) && !boardModel.areAllStonesInsideMill(color)) {
				LOG.info(msg("stone_cannot_be_removed_from_mill"));
			} else {
				board.removeStoneAt(clickPosition);
				LOG.info(isWhitesTurn() ? msg("white_took_stone") : msg("black_took_stone"));
				return optClickPosition;
			}
		}
		return OptionalInt.empty();
	}

	// Moving stones

	private OptionalInt supplyMoveStart() {
		if (Mouse.clicked()) {
			OptionalInt optStartPosition = board.findPosition(Mouse.getX(), Mouse.getY());
			if (optStartPosition.isPresent()) {
				int from = optStartPosition.getAsInt();
				if (boardModel.isEmpty(from)) {
					LOG.info(msg("stone_at_position_not_existing", from));
				} else if (!canJump() && !boardModel.hasEmptyNeighbor(from)) {
					LOG.info(msg("stone_at_position_cannot_move", from));
				} else {
					Optional<Stone> optStone = board.getStoneAt(from);
					if (!optStone.isPresent()) {
						LOG.info(msg("stone_at_position_not_existing", from));
					} else if (turn != optStone.get().getType()) {
						LOG.info(msg("stone_at_position_wrong_color", from));
					} else {
						return optStartPosition;
					}
				}
			}
		}
		return OptionalInt.empty();
	}

	private OptionalInt supplyMoveEnd() {
		int from = move.getFrom().getAsInt();

		// if target position is unique, use it
		if (!canJump() && boardModel.emptyNeighbors(from).count() == 1) {
			return boardModel.emptyNeighbors(from).findFirst();
		}

		// if move direction was specified and board position in that direction is empty, use it
		Optional<Direction> optMoveDirection = supplyMoveDirection();
		if (optMoveDirection.isPresent()) {
			Direction dir = optMoveDirection.get();
			OptionalInt optNeighbor = boardModel.neighbor(from, dir);
			if (optNeighbor.isPresent()) {
				int neighbor = optNeighbor.getAsInt();
				if (boardModel.isEmpty(neighbor)) {
					return OptionalInt.of(neighbor);
				}
			}
		}

		// if target position was selected with mouse click and move to that position is possible, use it
		if (Mouse.clicked()) {
			OptionalInt optClickPos = board.findPosition(Mouse.getX(), Mouse.getY());
			if (optClickPos.isPresent()) {
				int clickPos = optClickPos.getAsInt();
				if (!boardModel.hasStoneAt(clickPos) && (canJump() || boardModel.areNeighbors(from, clickPos))) {
					return optClickPos;
				}
			}
		}

		// no move end position could be determined
		return OptionalInt.empty();
	}

	private Optional<Direction> supplyMoveDirection() {
		/*@formatter:off*/
		return DIRECTION_KEYS.entrySet().stream()
			.filter(e -> Keyboard.keyPressedOnce(e.getValue()))
			.map(Map.Entry::getKey)
			.findAny();
		/*@formatter:on*/
	}

	private Vector2 supplyMoveVelocity() {
		int from = move.getFrom().getAsInt(), to = move.getTo().getAsInt();
		float speed = dist(board.centerPoint(from), board.centerPoint(to))
				/ app.pulse.secToTicks(app.settings.getAsFloat("seconds-per-move"));
		Direction dir = board.getModel().getDirection(from, to).get();
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
		return Vector2.nullVector();
	}

	// Drawing

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		board.draw(g);
		messageDisplay.hCenter(getWidth());
		messageDisplay.draw(g);
		drawStateSpecificInfo(g);
	}

	private void drawStateSpecificInfo(Graphics2D g) {
		switch (control.stateID()) {
		case PLACING:
			drawPlacingInfo(g);
			break;
		case MOVING:
			drawMovingInfo(g);
			break;
		default:
			break;
		}
	}

	private void drawPlacingInfo(Graphics2D g) {
		whiteStonesToPlaceCounter.draw(g);
		blackStonesToPlaceCounter.draw(g);
		if (control.mustRemoveStoneOfOpponent) {
			board.markRemovableStones(g, opponent());
		} else if (assistantOn) {
			board.markPositionsClosingMill(g, turn, Color.GREEN);
			board.markPositionsOpeningTwoMills(g, turn, Color.YELLOW);
			board.markPositionsClosingMill(g, opponent(), Color.RED);
		}
	}

	private void drawMovingInfo(Graphics2D g) {
		if (move.getFrom().isPresent()) {
			board.markPosition(g, move.getFrom().getAsInt(), Color.ORANGE);
		} else {
			board.markPossibleMoveStarts(g, turn, canJump());
			if (assistantOn) {
				board.markPositionFixingOpponent(g, turn, opponent(), Color.RED);
			}
		}
		if (control.mustRemoveStoneOfOpponent) {
			board.markRemovableStones(g, opponent());
		}
	}
}