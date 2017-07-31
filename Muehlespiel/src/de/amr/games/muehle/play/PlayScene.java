package de.amr.games.muehle.play;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.game.math.Vector2.dist;
import static de.amr.games.muehle.board.Direction.EAST;
import static de.amr.games.muehle.board.Direction.NORTH;
import static de.amr.games.muehle.board.Direction.SOUTH;
import static de.amr.games.muehle.board.Direction.WEST;
import static de.amr.games.muehle.board.StoneColor.BLACK;
import static de.amr.games.muehle.board.StoneColor.WHITE;
import static de.amr.games.muehle.play.GamePhase.GAME_OVER;
import static de.amr.games.muehle.play.GamePhase.MOVING;
import static de.amr.games.muehle.play.GamePhase.PLACING;
import static de.amr.games.muehle.play.GamePhase.STARTED;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Set;
import java.util.stream.IntStream;

import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.MillApp;
import de.amr.games.muehle.board.Board;
import de.amr.games.muehle.board.Direction;
import de.amr.games.muehle.board.Move;
import de.amr.games.muehle.board.Stone;
import de.amr.games.muehle.board.StoneColor;
import de.amr.games.muehle.mouse.Mouse;
import de.amr.games.muehle.ui.StonesPlacedIndicator;

/**
 * The play scene of the game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	private static final int NUM_STONES = 9;

	private final PlayControl control;
	private final Mouse mouse;

	private Board board;
	private StonesPlacedIndicator placedWhiteIndicator;
	private StonesPlacedIndicator placedBlackIndicator;
	private ScrollingText messageDisplay;

	private Move move;
	private StoneColor turn;
	private StoneColor winner;
	private int whiteStonesSet;
	private int blackStonesSet;
	private boolean mustRemoveOppositeStone;
	private boolean assistantOn;

	private class PlayControl extends StateMachine<GamePhase, String> {

		public PlayControl() {
			super("Mühlespiel Steuerung", GamePhase.class, STARTED);

			// STARTED

			state(STARTED).entry = s -> newGame();

			change(STARTED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(PLACING).entry = s -> {
				whiteStonesSet = 0;
				blackStonesSet = 0;
				mustRemoveOppositeStone = false;
				turn = WHITE;
				displayMessage(turn == WHITE ? "white_must_place" : "black_must_place");
			};

			state(PLACING).update = s -> {
				if (mustRemoveOppositeStone) {
					if (tryToRemoveStone(findClickPosition(), oppositeTurn())) {
						mustRemoveOppositeStone = false;
						nextTurn();
						displayMessage(turn == WHITE ? "white_must_place" : "black_must_place");
					}
				} else {
					int p = tryToPlaceStone(findClickPosition());
					if (p != -1) {
						if (board.isPositionInsideMill(p, turn)) {
							mustRemoveOppositeStone = true;
							displayMessage(turn == WHITE ? "white_must_take" : "black_must_take");
						} else {
							nextTurn();
							displayMessage(turn == WHITE ? "white_must_place" : "black_must_place");
						}
					}
				}
			};

			change(PLACING, MOVING, () -> blackStonesSet == NUM_STONES && !mustRemoveOppositeStone);

			// MOVING

			state(MOVING).entry = s -> {
				move = new Move(board, PlayScene.this::supplyMoveSpeed);
				displayMessage(turn == WHITE ? "white_at_move" : "black_at_move");
			};

			state(MOVING).update = s -> {
				if (mustRemoveOppositeStone) {
					if (tryToRemoveStone(findClickPosition(), oppositeTurn())) {
						mustRemoveOppositeStone = false;
						nextTurn();
						displayMessage(turn == WHITE ? "white_at_move" : "black_at_move");
					}
				} else {
					tryToMoveStone();
					if (move.isComplete()) {
						if (board.isPositionInsideMill(move.getTo(), turn)) {
							mustRemoveOppositeStone = true;
							displayMessage(turn == WHITE ? "white_must_take" : "black_must_take");
						} else {
							nextTurn();
							displayMessage(turn == WHITE ? "white_at_move" : "black_at_move");
						}
						move.reset();
					}
				}
			};

			change(MOVING, GAME_OVER, PlayScene.this::isGameOver);

			// GAME_OVER

			state(GAME_OVER).entry = s -> {
				winner = oppositeTurn();
				displayMessage(winner == WHITE ? "white_wins" : "black_wins");
			};

			change(GAME_OVER, STARTED, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}
	}

	public PlayScene(MillApp app) {
		super(app);
		control = new PlayControl();
		mouse = new Mouse();
		app.getShell().getCanvas().addMouseListener(mouse);
		setBgColor(Color.WHITE);
	}

	@Override
	public void init() {
		control.setLogger(LOG);
		control.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER)) {
			control.init();
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_H)) {
			assistantOn = !assistantOn;
			LOG.info("Help assistant is " + (assistantOn ? "on" : "off"));
		}
		mouse.poll();
		control.update();
		board.update();
	}

	private void newGame() {
		board = new Board(600, 600);
		board.hCenter(getWidth());
		board.tf.setY(50);

		placedWhiteIndicator = new StonesPlacedIndicator(WHITE, NUM_STONES, () -> whiteStonesSet);
		placedWhiteIndicator.tf.moveTo(50, getHeight() - 50);

		placedBlackIndicator = new StonesPlacedIndicator(BLACK, NUM_STONES, () -> blackStonesSet);
		placedBlackIndicator.tf.moveTo(getWidth() - 50, getHeight() - 50);

		messageDisplay = new ScrollingText();
		messageDisplay.setColor(Color.BLACK);
		messageDisplay.setFont(new Font("Sans", Font.PLAIN, 20));
		messageDisplay.tf.moveTo(0, getHeight() - 50);
		displayMessage("newgame");
	}

	private void displayMessage(String text, Object... args) {
		messageDisplay.setText(MessageFormat.format(app.messages.getString(text), args));
	}

	private void nextTurn() {
		turn = oppositeTurn();
	}

	private StoneColor oppositeTurn() {
		return turn == WHITE ? BLACK : WHITE;
	}

	// Placing

	private int findClickPosition() {
		return mouse.clicked() ? board.findPosition(mouse.getX(), mouse.getY()) : -1;
	}

	private int tryToPlaceStone(int p) {
		if (p == -1) {
			return -1;
		}
		if (board.hasStoneAt(p)) {
			LOG.info("An Mausklick-Position liegt bereits ein Stein");
			return -1;
		}
		if (turn == WHITE) {
			board.putStoneAt(p, WHITE);
			whiteStonesSet += 1;
		} else {
			board.putStoneAt(p, BLACK);
			blackStonesSet += 1;
		}
		return p;
	}

	private boolean tryToRemoveStone(int p, StoneColor color) {
		if (p == -1) {
			return false;
		}
		if (!board.hasStoneAt(p)) {
			LOG.info("Kein Stein an Klickposition");
			return false;
		}
		if (board.getStoneAt(p).getColor() != color) {
			LOG.info("Stein an Klickposition besitzt die falsche Farbe");
			return false;
		}
		if (board.isPositionInsideMill(p, color) && !board.areAllStonesInsideMill(color)) {
			LOG.info("Stein darf nicht aus Mühle entfernt werden, weil anderer Stein außerhalb Mühle existiert");
			return false;
		}
		board.removeStoneAt(p);
		LOG.info(turn + " hat gegnerischen Stein weggenommen");
		return true;
	}

	// Moving

	private boolean canJump() {
		return board.numStones(turn) == 3;
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
		if (!mouse.clicked())
			return;

		int from = board.findPosition(mouse.getX(), mouse.getY());
		if (from == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return;
		}
		if (!canJump() && !board.hasEmptyNeighbor(from)) {
			LOG.info("Stein an dieser Position kann nicht ziehen");
			return;
		}
		Stone stone = board.getStoneAt(from);
		if (stone == null) {
			LOG.info("Kein Stein an Klickposition gefunden");
			return;
		}
		if (turn != stone.getColor()) {
			LOG.info("Gegner ist am Zug");
			return;
		}
		move.setFrom(from);
	}

	private void supplyMoveEndPosition() {
		// unique target position?
		if (!canJump() && board.emptyNeighbors(move.getFrom()).count() == 1) {
			move.setTo(board.emptyNeighbors(move.getFrom()).findFirst().getAsInt());
			return;
		}
		// cursor key pressed?
		Direction dir = supplyMoveDirection();
		if (dir != null) {
			int to = board.findNeighbor(move.getFrom(), dir);
			if (to != -1 && board.isEmpty(to)) {
				move.setTo(to);
				return;
			}
		}
		// target position selected with mouse click?
		if (mouse.clicked()) {
			int to = board.findPosition(mouse.getX(), mouse.getY());
			if (to == -1 || board.hasStoneAt(to)) {
				return;
			}
			if (canJump() || board.areNeighbors(move.getFrom(), to)) {
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

	private boolean isGameOver() {
		if (board.numStones(turn) == 2) {
			return true;
		}
		if (canJump()) {
			return false;
		}
		return board.cannotMoveStones(turn);
	}

	// Drawing

	@Override
	public void draw(Graphics2D g) {
		g.setColor(getBgColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		board.draw(g);
		drawStateInformation(g);
	}

	private void drawStateInformation(Graphics2D g) {

		messageDisplay.hCenter(getWidth());
		messageDisplay.draw(g);

		if (control.is(PLACING)) {
			placedWhiteIndicator.draw(g);
			placedBlackIndicator.draw(g);
			highlightStone(g, turn == WHITE ? placedWhiteIndicator : placedBlackIndicator);
			if (mustRemoveOppositeStone) {
				markRemovableStones(g);
			} else {
				if (assistantOn) {
					markPositionsClosingMills(g, turn, Color.GREEN);
					markPositionsOpeningTwoMills(g, turn, Color.YELLOW);
					markPositionsClosingMills(g, oppositeTurn(), Color.RED);
				}
			}
			return;
		}

		if (control.is(MOVING)) {
			if (move.getFrom() == -1) {
				markPossibleMoveStarts(g);
				if (assistantOn) {
					markPositionFixingOpponent(g, Color.RED);
				}
			} else {
				markPosition(g, move.getFrom(), Color.ORANGE, 10);
			}
			if (mustRemoveOppositeStone) {
				markRemovableStones(g);
			}
			return;
		}
	}

	private void markPositionsOpeningTwoMills(Graphics2D g, StoneColor stoneColor, Color color) {
		board.positionsForOpening2Mills(stoneColor).forEach(p -> markPosition(g, p, color, 10));
	}

	private void markPositionsClosingMills(Graphics2D g, StoneColor stoneColor, Color color) {
		board.positionsForClosingMill(stoneColor).forEach(p -> markPosition(g, p, color, 10));
	}

	private void markPositionFixingOpponent(Graphics2D g, Color color) {
		Set<Integer> oppFreePositions = board.freeNeighbors(oppositeTurn());
		if (oppFreePositions.size() == 1) {
			Set<Integer> ownFreePositions = board.freeNeighbors(turn);
			for (int p : oppFreePositions) {
				if (ownFreePositions.contains(p)) {
					markPosition(g, p, color, 10);
				}
			}
		}
	}

	private void markPossibleMoveStarts(Graphics2D g) {
		IntStream startPositions = canJump() ? board.positionsWithStone(turn) : board.allMovableStonePositions(turn);
		startPositions.forEach(p -> {
			markPosition(g, p, Color.GREEN, 10);
		});
		startPositions.close();
	}

	private void markRemovableStones(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		StoneColor colorToRemove = oppositeTurn();
		boolean allInMill = board.areAllStonesInsideMill(colorToRemove);
		board.positionsWithStone(colorToRemove).filter(p -> allInMill || !board.isPositionInsideMill(p, oppositeTurn()))
				.forEach(p -> {
					Stone stone = board.getStoneAt(p);
					g.translate(board.tf.getX() + stone.tf.getX() - stone.getWidth() / 2,
							board.tf.getY() + stone.tf.getY() - stone.getHeight() / 2);
					g.setColor(Color.RED);
					g.drawLine(0, 0, stone.getWidth(), stone.getHeight());
					g.drawLine(0, stone.getHeight(), stone.getWidth(), 0);
					g.translate(-board.tf.getX() - stone.tf.getX() + stone.getWidth() / 2,
							-board.tf.getY() - stone.tf.getY() + stone.getHeight() / 2);
				});
	}

	private void markPosition(Graphics2D g, int p, Color color, int markerSize) {
		Vector2 center = board.centerPoint(p);
		g.translate(board.tf.getX(), board.tf.getY());
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(color);
		g.fillOval(round(center.x) - markerSize / 2, round(center.y) - markerSize / 2, markerSize, markerSize);
		g.translate(-board.tf.getX(), -board.tf.getY());
	}

	private void highlightStone(Graphics2D g, Stone stone) {
		g.translate(stone.tf.getX() - Stone.radius, stone.tf.getY() - Stone.radius);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(4));
		g.drawOval(0, 0, 2 * Stone.radius, 2 * Stone.radius);
		g.translate(-stone.tf.getX() + Stone.radius, -stone.tf.getY() + Stone.radius);
	}
}