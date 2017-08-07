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
import de.amr.games.muehle.board.BoardGraph;
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

	private final PlayControl control;

	private BoardGraph boardGraph;
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

		private boolean mustRemoveOpponentStone;

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
				mustRemoveOpponentStone = false;
				setPlacingTurn(WHITE);
			};

			state(PLACING).update = s -> {
				if (mustRemoveOpponentStone) {
					tryToRemoveStone(opponent()).ifPresent(pos -> {
						mustRemoveOpponentStone = false;
						setPlacingTurn(opponent());
					});
				} else {
					tryToPlaceStone().ifPresent(pos -> {
						if (boardGraph.isPositionInsideMill(pos, turn)) {
							mustRemoveOpponentStone = true;
							displayMessage(isWhitesTurn() ? "white_must_take" : "black_must_take");
						} else {
							setPlacingTurn(opponent());
						}
					});
				}
			};

			change(PLACING, MOVING, () -> blackStonesPlaced == NUM_STONES && !mustRemoveOpponentStone);

			// MOVING

			state(MOVING).entry = s -> {
				move = new Move(board, PlayScene.this::supplyMoveSpeed, PlayScene.this::canJump);
				setMovingTurn(turn);
			};

			state(MOVING).update = s -> {
				if (mustRemoveOpponentStone) {
					tryToRemoveStone(opponent()).ifPresent(pos -> {
						mustRemoveOpponentStone = false;
						setMovingTurn(opponent());
					});
				} else {
					tryToMoveStone().ifPresent(to -> {
						if (boardGraph.isPositionInsideMill(to, turn)) {
							mustRemoveOpponentStone = true;
							displayMessage(isWhitesTurn() ? "white_must_take" : "black_must_take");
						} else {
							setMovingTurn(opponent());
						}
						move.init();
					});
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

		boardGraph = new BoardGraph();

		board = new Board(boardGraph, 600, 600);
		board.hCenter(getWidth());
		board.tf.setY(50);

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

	private void setPlacingTurn(StoneType type) {
		turn = type;
		displayMessage(isWhitesTurn() ? "white_must_place" : "black_must_place");
		whiteStonesToPlaceCounter.setSelected(isWhitesTurn());
		blackStonesToPlaceCounter.setSelected(!isWhitesTurn());
	}

	private void setMovingTurn(StoneType type) {
		turn = type;
		displayMessage(isWhitesTurn() ? "white_must_move" : "black_must_move");
	}

	private void setWinner(StoneType type) {
		winner = type;
		displayMessage(winner == WHITE ? "white_wins" : "black_wins");
	}

	private boolean isGameOver() {
		return boardGraph.stoneCount(turn) == 2 || (!canJump() && boardGraph.cannotMoveStones(turn));
	}

	private boolean canJump() {
		return boardGraph.stoneCount(turn) == 3;
	}

	// Placing stones

	private OptionalInt findMouseClickPosition() {
		return Mouse.clicked() ? board.findPosition(Mouse.getX(), Mouse.getY()) : OptionalInt.empty();
	}

	private OptionalInt tryToPlaceStone() {
		OptionalInt optClickPosition = findMouseClickPosition();
		if (optClickPosition.isPresent()) {
			int clickPosition = optClickPosition.getAsInt();
			if (boardGraph.hasStoneAt(clickPosition)) {
				LOG.info(msg("stone_at_position", clickPosition));
				return OptionalInt.empty();
			}
			if (isWhitesTurn()) {
				board.putStoneAt(clickPosition, WHITE);
				whiteStonesPlaced += 1;
			} else {
				board.putStoneAt(clickPosition, BLACK);
				blackStonesPlaced += 1;
			}
		}
		return optClickPosition;
	}

	private OptionalInt tryToRemoveStone(StoneType color) {
		OptionalInt optClickPosition = findMouseClickPosition();
		if (optClickPosition.isPresent()) {
			int clickPosition = optClickPosition.getAsInt();
			if (!boardGraph.hasStoneAt(clickPosition)) {
				LOG.info(msg("stone_at_position_not_existing", clickPosition));
			} else if (boardGraph.getStoneAt(clickPosition) != color) {
				LOG.info(msg("stone_at_position_wrong_color", clickPosition));
			} else if (boardGraph.isPositionInsideMill(clickPosition, color) && !boardGraph.areAllStonesInsideMill(color)) {
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

	private OptionalInt tryToMoveStone() {
		if (!move.getFrom().isPresent()) {
			supplyMoveStartPosition().ifPresent(from -> move.setFrom(from));
		} else if (!move.getTo().isPresent()) {
			supplyMoveEndPosition().ifPresent(to -> move.setTo(to));
		} else {
			move.update();
		}
		return move.isComplete() ? move.getTo() : OptionalInt.empty();
	}

	private OptionalInt supplyMoveStartPosition() {
		if (Mouse.clicked()) {
			OptionalInt optStartPosition = board.findPosition(Mouse.getX(), Mouse.getY());
			if (optStartPosition.isPresent()) {
				int from = optStartPosition.getAsInt();
				if (!canJump() && !boardGraph.hasEmptyNeighbor(from)) {
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

	private OptionalInt supplyMoveEndPosition() {
		if (!move.getFrom().isPresent()) {
			return OptionalInt.empty();
		}
		int from = move.getFrom().getAsInt();

		// if target position is unique, use it
		if (!canJump() && boardGraph.emptyNeighbors(from).count() == 1) {
			return boardGraph.emptyNeighbors(from).findFirst();
		}

		// if move direction specified and position in that direction is empty, use it
		Optional<Direction> optMoveDirection = supplyMoveDirection();
		if (optMoveDirection.isPresent()) {
			Direction dir = optMoveDirection.get();
			OptionalInt optNeighbor = boardGraph.neighbor(from, dir);
			if (optNeighbor.isPresent()) {
				int neighbor = optNeighbor.getAsInt();
				if (boardGraph.isEmpty(neighbor)) {
					return OptionalInt.of(neighbor);
				}
			}
		}

		// if target position selected with mouse click and can move to that position, use it
		if (Mouse.clicked()) {
			OptionalInt optClickPos = board.findPosition(Mouse.getX(), Mouse.getY());
			if (optClickPos.isPresent()) {
				int clickPos = optClickPos.getAsInt();
				if (!boardGraph.hasStoneAt(clickPos) && (canJump() || boardGraph.areNeighbors(from, clickPos))) {
					return OptionalInt.of(clickPos);
				}
			}
		}

		// no end position
		return OptionalInt.empty();
	}

	private Optional<Direction> supplyMoveDirection() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_UP)) {
			return Optional.of(NORTH);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_RIGHT)) {
			return Optional.of(EAST);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_DOWN)) {
			return Optional.of(SOUTH);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_LEFT)) {
			return Optional.of(WEST);
		}
		return Optional.empty();
	}

	private double supplyMoveSpeed() {
		Vector2 centerFrom = board.centerPoint(move.getFrom().getAsInt());
		Vector2 centerTo = board.centerPoint(move.getTo().getAsInt());
		return dist(centerFrom, centerTo) / app.pulse.secToTicks(app.settings.getAsFloat("seconds-per-move"));
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
		if (control.mustRemoveOpponentStone) {
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
		if (control.mustRemoveOpponentStone) {
			board.markRemovableStones(g, opponent());
		}
	}
}