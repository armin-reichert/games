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

		private boolean isGameOver() {
			if (boardGraph.stoneCount(turn) == 2) {
				return true;
			}
			if (canJump()) {
				return false;
			}
			return boardGraph.cannotMoveStones(turn);
		}

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
					if (tryToRemoveStone(opponent())) {
						mustRemoveOpponentStone = false;
						setPlacingTurn(opponent());
					}
				} else {
					int p = tryToPlaceStone();
					if (p != -1) {
						if (boardGraph.isPositionInsideMill(p, turn)) {
							mustRemoveOpponentStone = true;
							displayMessage(isWhitesTurn() ? "white_must_take" : "black_must_take");
						} else {
							setPlacingTurn(opponent());
						}
					}
				}
			};

			change(PLACING, MOVING, () -> blackStonesPlaced == NUM_STONES && !mustRemoveOpponentStone);

			// MOVING

			state(MOVING).entry = s -> {
				move = new Move(board, PlayScene.this::supplyMoveSpeed);
				setMovingTurn(turn);
			};

			state(MOVING).update = s -> {
				if (mustRemoveOpponentStone) {
					boolean removed = tryToRemoveStone(opponent());
					if (removed) {
						mustRemoveOpponentStone = false;
						setMovingTurn(opponent());
					}
				} else {
					tryToMoveStone();
					if (move.isComplete()) {
						if (boardGraph.isPositionInsideMill(move.getTo(), turn)) {
							mustRemoveOpponentStone = true;
							displayMessage(isWhitesTurn() ? "white_must_take" : "black_must_take");
						} else {
							setMovingTurn(opponent());
						}
						move.clear();
					}
				}
			};

			change(MOVING, GAME_OVER, this::isGameOver);

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
		boardGraph = new BoardGraph();

		board = new Board(boardGraph, 600, 600);
		board.hCenter(getWidth());
		board.tf.setY(50);

		whiteStonesToPlaceCounter = new StonesCounter(WHITE, () -> NUM_STONES - whiteStonesPlaced);
		whiteStonesToPlaceCounter.tf.moveTo(50, getHeight() - 50);

		blackStonesToPlaceCounter = new StonesCounter(BLACK, () -> NUM_STONES - blackStonesPlaced);
		blackStonesToPlaceCounter.tf.moveTo(getWidth() - 50, getHeight() - 50);

		Font msgFont = Assets.storeFont("message-font", "fonts/Cookie-Regular.ttf", 40, Font.PLAIN);
		messageDisplay = new ScrollingText();
		messageDisplay.setColor(Color.BLUE);
		messageDisplay.setFont(msgFont);
		messageDisplay.tf.moveTo(0, getHeight() - 80);

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
		whiteStonesToPlaceCounter.setHighlighted(isWhitesTurn());
		blackStonesToPlaceCounter.setHighlighted(!isWhitesTurn());
	}

	private void setMovingTurn(StoneType type) {
		turn = type;
		displayMessage(isWhitesTurn() ? "white_must_move" : "black_must_move");
	}

	private void setWinner(StoneType type) {
		winner = type;
		displayMessage(winner == WHITE ? "white_wins" : "black_wins");
	}

	// Placing

	private int findClickPosition() {
		return Mouse.clicked() ? board.findPosition(Mouse.getX(), Mouse.getY()) : -1;
	}

	private int tryToPlaceStone() {
		int p = findClickPosition();
		if (p == -1) {
			return -1;
		}
		if (boardGraph.hasStoneAt(p)) {
			LOG.info(msg("stone_at_position", p));
			return -1;
		}
		if (isWhitesTurn()) {
			board.putStoneAt(p, WHITE);
			whiteStonesPlaced += 1;
		} else {
			board.putStoneAt(p, BLACK);
			blackStonesPlaced += 1;
		}
		return p;
	}

	private boolean tryToRemoveStone(StoneType color) {
		int p = findClickPosition();
		if (p == -1) {
			return false;
		}
		if (!boardGraph.hasStoneAt(p)) {
			LOG.info(msg("stone_at_position_not_existing"));
			return false;
		}
		if (boardGraph.getStoneAt(p) != color) {
			LOG.info(msg("stone_at_position_wrong_color"));
			return false;
		}
		if (boardGraph.isPositionInsideMill(p, color) && !boardGraph.areAllStonesInsideMill(color)) {
			LOG.info(msg("stone_cannot_be_removed_from_mill"));
			return false;
		}
		board.removeStoneAt(p);
		LOG.info(isWhitesTurn() ? msg("white_took_stone") : msg("black_took_stone"));
		return true;
	}

	// Moving

	private boolean canJump() {
		return boardGraph.stoneCount(turn) == 3;
	}

	private void tryToMoveStone() {
		if (move.getFrom() == -1) {
			supplyMoveStartPosition();
		} else if (move.getTo() == -1) {
			supplyMoveEndPosition();
		} else {
			move.execute();
		}
	}

	private void supplyMoveStartPosition() {
		if (!Mouse.clicked())
			return;

		int from = board.findPosition(Mouse.getX(), Mouse.getY());
		if (from == -1) {
			LOG.info(msg("no_position_identified"));
			return;
		}
		if (!canJump() && !boardGraph.hasEmptyNeighbor(from)) {
			LOG.info(msg("stone_at_position_cannot_move", from));
			return;
		}
		Stone stone = board.getStoneAt(from);
		if (stone == null) {
			LOG.info(msg("stone_at_position_not_existing", from));
			return;
		}
		if (turn != stone.getColor()) {
			LOG.info(msg("stone_at_position_wrong_color", from));
			return;
		}
		move.setFrom(from);
	}

	private void supplyMoveEndPosition() {
		// unique target position?
		if (!canJump() && boardGraph.emptyNeighbors(move.getFrom()).count() == 1) {
			move.setTo(boardGraph.emptyNeighbors(move.getFrom()).findFirst().getAsInt());
			return;
		}
		// cursor key pressed?
		Direction dir = supplyMoveDirection();
		if (dir != null) {
			int to = boardGraph.neighbor(move.getFrom(), dir);
			if (to != -1 && boardGraph.isEmpty(to)) {
				move.setTo(to);
				return;
			}
		}
		// target position selected with mouse click?
		if (Mouse.clicked()) {
			int to = board.findPosition(Mouse.getX(), Mouse.getY());
			if (to == -1 || boardGraph.hasStoneAt(to)) {
				return;
			}
			if (canJump() || boardGraph.areNeighbors(move.getFrom(), to)) {
				move.setTo(to);
				return;
			}
		}
	}

	private Direction supplyMoveDirection() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_UP)) {
			return NORTH;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_RIGHT)) {
			return EAST;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_DOWN)) {
			return SOUTH;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_LEFT)) {
			return WEST;
		}
		return null;
	}

	private double supplyMoveSpeed() {
		Vector2 centerFrom = board.centerPoint(move.getFrom());
		Vector2 centerTo = board.centerPoint(move.getTo());
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
		if (move.getFrom() == -1) {
			board.markPossibleMoveStarts(g, turn, canJump());
			if (assistantOn) {
				board.markPositionFixingOpponent(g, turn, opponent(), Color.RED);
			}
		} else {
			board.markPosition(g, move.getFrom(), Color.ORANGE);
		}
		if (control.mustRemoveOpponentStone) {
			board.markRemovableStones(g, opponent());
		}
	}
}