package de.amr.games.muehle;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.Direction.EAST;
import static de.amr.games.muehle.Direction.NORTH;
import static de.amr.games.muehle.Direction.SOUTH;
import static de.amr.games.muehle.Direction.WEST;
import static de.amr.games.muehle.GamePhase.GAME_OVER;
import static de.amr.games.muehle.GamePhase.MOVING;
import static de.amr.games.muehle.GamePhase.PLACING;
import static de.amr.games.muehle.GamePhase.STARTED;
import static de.amr.games.muehle.StoneColor.BLACK;
import static de.amr.games.muehle.StoneColor.WHITE;
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

	private final int NUM_STONES = 9;
	private final float STONE_SPEED = 3f;

	private final Mouse mouse;

	private Board board;
	private Move move;
	private ScrollingText startText;

	private StoneColor turn;
	private StoneColor winner;
	private int numWhiteStonesSet;
	private int numBlackStonesSet;
	private boolean mustRemoveOppositeStone;

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
					if (tryToRemoveStone(oppositeTurn())) {
						mustRemoveOppositeStone = false;
						nextTurn();
					}
				} else {
					int placed = tryToPlaceStone();
					if (placed != -1) {
						if (board.isInsideMill(placed, turn)) {
							mustRemoveOppositeStone = true;
						} else {
							nextTurn();
						}
					}
				}
			};

			change(PLACING, MOVING, () -> numBlackStonesSet == NUM_STONES && !mustRemoveOppositeStone);

			// MOVING

			state(MOVING).entry = s -> {
				move = new Move(board, STONE_SPEED);
				move.startPositionSupplier = PlayScene.this::supplyMoveStartPosition;
				move.directionSupplier = PlayScene.this::supplyMoveDirection;
				move.init();
			};

			state(MOVING).update = s -> {
				if (mustRemoveOppositeStone) {
					if (tryToRemoveStone(oppositeTurn())) {
						mustRemoveOppositeStone = false;
						nextTurn();
					}
				} else {
					move.update();
				}
			};

			change(MOVING, GAME_OVER, PlayScene.this::isGameOver);

			change(MOVING, MOVING, () -> move.isComplete(), (s, t) -> {
				if (board.isInsideMill(move.getTo(), turn)) {
					mustRemoveOppositeStone = true;
					LOG.info(turn + " hat Mühle geschlossen und muss Stein wegnehmen");
				} else {
					nextTurn();
				}
				move.init();
			});

			state(GamePhase.GAME_OVER).entry = s -> {
				winner = oppositeTurn();
				LOG.info("Gewinner ist " + winner);
			};

		}
	}

	public PlayScene(MillApp app) {
		super(app);
		setBgColor(Color.WHITE);
		mouse = new Mouse();
		app.getShell().getCanvas().addMouseListener(mouse);
	}

	private int findBoardPosition(int x, int y) {
		int brettX = Math.abs(Math.round(x - board.tf.getX()));
		int brettY = Math.abs(Math.round(y - board.tf.getY()));
		return board.findNearestPosition(brettX, brettY, board.getWidth() / 18);
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
	}

	private void nextTurn() {
		turn = oppositeTurn();
		LOG.info("Jetzt ist " + turn + " an der Reihe");
	}

	private StoneColor oppositeTurn() {
		return turn == WHITE ? BLACK : WHITE;
	}

	// Placing

	private int tryToPlaceStone() {
		if (!mouse.clicked())
			return -1;

		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p == -1) {
			LOG.info("Keine passende Position zu Mausklick gefunden");
			return -1;
		}
		if (board.hasStoneAt(p)) {
			LOG.info("An Mausklick-Position ist bereits ein Stein");
			return -1;
		}
		// An leerer Position p Stein setzen:
		if (turn == WHITE) {
			board.putStoneAt(p, WHITE);
			numWhiteStonesSet += 1;
		} else {
			board.putStoneAt(p, BLACK);
			numBlackStonesSet += 1;
		}
		return p;
	}

	private boolean tryToRemoveStone(StoneColor color) {
		if (!mouse.clicked())
			return false;

		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
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
			int offset = 5;
			Stone whiteStone = new Stone(StoneColor.WHITE);
			whiteStone.tf.moveTo(50, getHeight() - 50);
			whiteStone.draw(g);
			g.setColor(Color.BLACK);
			g.drawString(String.valueOf(NUM_STONES - numWhiteStonesSet), (int) whiteStone.tf.getX() - offset,
					(int) whiteStone.tf.getY() + offset);
			Stone blackStone = new Stone(StoneColor.BLACK);
			blackStone.tf.moveTo(getWidth() - 50, getHeight() - 50);
			blackStone.draw(g);
			g.setColor(Color.WHITE);
			g.drawString(String.valueOf(NUM_STONES - numBlackStonesSet), (int) blackStone.tf.getX() - offset,
					(int) blackStone.tf.getY() + offset);
			if (turn == WHITE) {
				highlightStone(g, whiteStone);
			} else {
				highlightStone(g, blackStone);
			}
			return;
		}
		if (playControl.is(MOVING)) {
			if (move.getFrom() != -1) {
				markPosition(g, move.getFrom(), Color.GREEN, 10);
			}
			if (move.getTo() != -1) {
				markPosition(g, move.getTo(), Color.RED, 10);
			}
			String text = (turn == WHITE ? "Weiß" : "Schwarz") + " am Zug";
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}
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