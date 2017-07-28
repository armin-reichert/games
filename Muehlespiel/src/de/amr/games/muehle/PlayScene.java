package de.amr.games.muehle;

import static de.amr.easy.game.Application.LOG;
import static de.amr.easy.game.math.Vector2.dist;
import static de.amr.games.muehle.Direction.EAST;
import static de.amr.games.muehle.Direction.NORTH;
import static de.amr.games.muehle.Direction.SOUTH;
import static de.amr.games.muehle.Direction.WEST;
import static de.amr.games.muehle.GamePhase.GAME_OVER;
import static de.amr.games.muehle.GamePhase.PLACING;
import static de.amr.games.muehle.GamePhase.PLAYING;
import static de.amr.games.muehle.GamePhase.STARTED;
import static de.amr.games.muehle.StoneColor.BLACK;
import static de.amr.games.muehle.StoneColor.WHITE;
import static java.lang.Math.abs;
import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.mouse.Mouse;

/**
 * The play scene of the game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<MillApp> {

	private static final int NUM_STONES = 9;
	private static final float SECONDS_PER_MOVE = 1f;

	private final Mouse mouse;

	private Board board;
	private Move move;
	private ScrollingText startText;
	private ScrollingText winnerText;

	private StoneColor turn;
	private StoneColor winner;
	private int numWhiteStonesSet;
	private int numBlackStonesSet;
	private boolean mustRemoveOppositeStone;

	private StonesPlacedIndicator placedWhiteIndicator;
	private StonesPlacedIndicator placedBlackIndicator;

	private final PlayControl playControl = new PlayControl();

	private class PlayControl extends StateMachine<GamePhase, String> {

		public PlayControl() {
			super("Mühlespiel Steuerung", GamePhase.class, STARTED);

			// STARTED

			state(STARTED).entry = s -> resetGame();

			state(STARTED).exit = s -> {
				startText.visibility = () -> false;
			};

			change(STARTED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(PLACING).entry = s -> {
				turn = WHITE;
				numWhiteStonesSet = 0;
				numBlackStonesSet = 0;
				mustRemoveOppositeStone = false;
			};

			state(PLACING).update = s -> {
				if (mustRemoveOppositeStone) {
					if (tryToRemoveStone(findClickPosition(), oppositeTurn())) {
						mustRemoveOppositeStone = false;
						nextTurn();
					}
				} else {
					int p = tryToPlaceStone(findClickPosition());
					if (p != -1) {
						if (board.isInsideMill(p, turn)) {
							mustRemoveOppositeStone = true;
						} else {
							nextTurn();
						}
					}
				}
			};

			change(PLACING, PLAYING, () -> numBlackStonesSet == NUM_STONES && !mustRemoveOppositeStone);

			// MOVING

			state(PLAYING).entry = s -> {
				move = new Move(board);
				move.startPositionSupplier = PlayScene.this::supplyMoveStartPosition;
				move.directionSupplier = PlayScene.this::supplyMoveDirection;
				move.speedSupplier = PlayScene.this::supplyMoveSpeed;
				move.init();
			};

			state(PLAYING).update = s -> {
				if (mustRemoveOppositeStone) {
					if (tryToRemoveStone(findClickPosition(), oppositeTurn())) {
						mustRemoveOppositeStone = false;
						nextTurn();
					}
				} else {
					move.update();
				}
			};

			change(PLAYING, GAME_OVER, PlayScene.this::isGameOver);

			change(PLAYING, PLAYING, () -> move.isComplete(), (s, t) -> {
				if (board.isInsideMill(move.getTo(), turn)) {
					mustRemoveOppositeStone = true;
					LOG.info(turn + " hat Mühle geschlossen und muss Stein wegnehmen");
				} else {
					nextTurn();
				}
				move.init();
			});

			// GAME_OVER

			state(GAME_OVER).entry = s -> {
				winner = oppositeTurn();
				LOG.info("Gewinner ist " + winner);
			};

			change(GAME_OVER, STARTED, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

		}
	}

	public PlayScene(MillApp app) {
		super(app);
		mouse = new Mouse();
		app.getShell().getCanvas().addMouseListener(mouse);
		setBgColor(Color.WHITE);
		placedWhiteIndicator = new StonesPlacedIndicator(WHITE, NUM_STONES, () -> numWhiteStonesSet);
		placedWhiteIndicator.tf.moveTo(50, getHeight() - 50);
		placedBlackIndicator = new StonesPlacedIndicator(BLACK, NUM_STONES, () -> numBlackStonesSet);
		placedBlackIndicator.tf.moveTo(getWidth() - 50, getHeight() - 50);
	}

	@Override
	public void init() {
		playControl.setLogger(Application.LOG);
		playControl.init();
	}

	@Override
	public void update() {
		mouse.poll();
		playControl.update();
		board.update();
	}

	private void resetGame() {
		board = new Board(600, 600);
		board.center(getWidth(), getHeight());
		app.entities.add(board);

		startText = new ScrollingText();
		startText.setColor(Color.BLACK);
		startText.setFont(new Font("Sans", Font.PLAIN, 20));
		startText.setText("Drücke SPACE zum Start");
		startText.tf.moveTo(0, getHeight() - 40);
		app.entities.add(startText);

		winnerText = new ScrollingText();
		winnerText.setColor(Color.BLACK);
		winnerText.setFont(new Font("Sans", Font.PLAIN, 20));
		winnerText.setText("Kein Gewinner");
		winnerText.tf.moveTo(0, getHeight() - 40);
		app.entities.add(winnerText);
	}

	private void nextTurn() {
		turn = oppositeTurn();
	}

	private StoneColor oppositeTurn() {
		return turn == WHITE ? BLACK : WHITE;
	}

	// Placing

	private int findBoardPosition(int x, int y) {
		int boardX = abs(round(x - board.tf.getX()));
		int boardY = abs(round(y - board.tf.getY()));
		return board.findNearestPosition(boardX, boardY, board.getWidth() / 18);
	}

	private int findClickPosition() {
		if (mouse.clicked()) {
			return findBoardPosition(mouse.getX(), mouse.getY());
		}
		return -1;
	}

	private int tryToPlaceStone(int p) {
		if (p == -1) {
			LOG.info("Keine gültige Brettposition");
			return -1;
		}
		if (board.hasStoneAt(p)) {
			LOG.info("An Mausklick-Position ist bereits ein Stein");
			return -1;
		}
		if (turn == WHITE) {
			board.putStoneAt(p, WHITE);
			numWhiteStonesSet += 1;
		} else {
			board.putStoneAt(p, BLACK);
			numBlackStonesSet += 1;
		}
		return p;
	}

	private boolean tryToRemoveStone(int p, StoneColor color) {
		if (p == -1) {
			LOG.info("Keine gültige Brettposition");
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
		if (board.isInsideMill(p, color) && !board.allStonesOfColorInsideMills(color)) {
			LOG.info("Stein darf nicht aus Mühle entfernt werden, weil anderer Stein außerhalb Mühle existiert");
			return false;
		}
		board.removeStoneAt(p);
		return true;
	}

	// Moving

	private int supplyMoveStartPosition() {
		if (!mouse.clicked())
			return -1;

		int from = findBoardPosition(mouse.getX(), mouse.getY());
		if (from == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return -1;
		}
		Stone stone = board.getStoneAt(from);
		if (stone == null) {
			LOG.info("Kein Stein an Klickposition gefunden");
			return -1;
		}
		if (turn != stone.getColor()) {
			LOG.info(stone.getColor() + " ist nicht am Zug");
			return -1;
		}
		if (!board.hasEmptyNeighbor(from)) {
			LOG.info("Stein an dieser Position kann nicht ziehen");
			return -1;
		}
		return from;
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
		return dist(centerFrom, centerTo) / app.pulse.secToTicks(SECONDS_PER_MOVE);
	}

	private boolean isGameOver() {
		return board.numStones(turn) == 2 || board.cannotMove(turn);
	}

	// Drawing

	@Override
	public void draw(Graphics2D g) {
		g.setColor(getBgColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		board.draw(g);
		drawStatus(g);
	}

	private void drawStatus(Graphics2D g) {
		if (playControl.is(STARTED)) {
			startText.hCenter(getWidth());
			startText.draw(g);
			return;
		}

		if (playControl.is(PLACING)) {
			placedWhiteIndicator.draw(g);
			placedBlackIndicator.draw(g);
			highlightStone(g, turn == WHITE ? placedWhiteIndicator : placedBlackIndicator);
			if (mustRemoveOppositeStone) {
				markRemovableStones(g);
			}
			return;
		}

		if (playControl.is(PLAYING)) {
			if (move.getFrom() != -1) {
				markPosition(g, move.getFrom(), Color.ORANGE, 10);
			} else {
				markAllPossibleMoveStarts(g);
			}
			if (mustRemoveOppositeStone) {
				markRemovableStones(g);
			}
			String text = (turn == WHITE ? "Weiß" : "Schwarz") + " am Zug";
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}

		if (playControl.is(GAME_OVER)) {
			winnerText.setText(winner == WHITE ? "Weiß gewinnt" : "Schwarz gewinnt");
			winnerText.hCenter(getWidth());
			winnerText.draw(g);
			return;
		}
	}

	private void markAllPossibleMoveStarts(Graphics2D g) {
		board.allMovableStonePositions(turn).forEach(p -> {
			markPosition(g, p, Color.GREEN, 10);
		});
	}

	private void markRemovableStones(Graphics2D g) {
		StoneColor colorToRemove = oppositeTurn();
		boolean allInMill = board.allStonesOfColorInsideMills(colorToRemove);
		board.positions(colorToRemove).filter(p -> allInMill || !board.isInsideMill(p, oppositeTurn())).forEach(p -> {
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

	private void markPosition(Graphics2D gc, int p, Color color, int markerSize) {
		Graphics2D g = (Graphics2D) gc.create();
		Vector2 center = board.centerPoint(p);
		g.translate(board.tf.getX(), board.tf.getY());
		g.setColor(color);
		g.fillOval(round(center.x) - markerSize / 2, round(center.y) - markerSize / 2, markerSize, markerSize);
		g.dispose();
	}

	private void highlightStone(Graphics2D gc, Stone stone) {
		Graphics2D g = (Graphics2D) gc.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate(stone.tf.getX() - Stone.radius, stone.tf.getY() - Stone.radius);
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(4));
		g.drawOval(0, 0, 2 * Stone.radius, 2 * Stone.radius);
		g.translate(-stone.tf.getX() + Stone.radius, -stone.tf.getY() + Stone.radius);
		g.dispose();
	}
}