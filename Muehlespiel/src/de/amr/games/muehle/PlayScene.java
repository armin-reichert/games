package de.amr.games.muehle;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.Direction.EAST;
import static de.amr.games.muehle.Direction.NORTH;
import static de.amr.games.muehle.Direction.SOUTH;
import static de.amr.games.muehle.Direction.WEST;
import static de.amr.games.muehle.GamePhase.INITIALIZED;
import static de.amr.games.muehle.GamePhase.MOVING;
import static de.amr.games.muehle.GamePhase.PLACING;
import static de.amr.games.muehle.StoneColor.BLACK;
import static de.amr.games.muehle.StoneColor.WHITE;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.mouse.Mouse;

public class PlayScene extends Scene<MillApp> {

	private final int NUM_STONES = 6;

	private final Mouse mouse;

	private Board board;
	private ScrollingText startText;

	private StoneColor turn;
	private int numWhiteStonesSet;
	private int numBlackStonesSet;
	private boolean mustRemoveOppositeStone;

	// stone movement
	private Stone movingStone;
	private int moveStartPosition;
	private int moveEndPosition;
	private Direction moveDirection;

	private float speed = 3;

	private final PlayControl playControl = new PlayControl();

	private class PlayControl extends StateMachine<GamePhase, String> {

		public PlayControl() {
			super("Mühlespiel Steuerung", GamePhase.class, GamePhase.INITIALIZED);

			// INITIALIZED

			state(INITIALIZED).entry = s -> resetGame();

			state(INITIALIZED).exit = s -> {
				startText.visibility = () -> false;
			};

			change(INITIALIZED, PLACING, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// PLACING

			state(PLACING).entry = s -> {
				turn = WHITE;
				numWhiteStonesSet = 0;
				numBlackStonesSet = 0;
				mustRemoveOppositeStone = false;
			};

			state(PLACING).update = s -> {
				if (mustRemoveOppositeStone) {
					removeStone();
				} else {
					placeStone();
				}
			};

			change(PLACING, MOVING, () -> numBlackStonesSet == NUM_STONES && !mustRemoveOppositeStone);

			// MOVING

			state(MOVING).entry = s -> {
				moveControl.init();
				moveControl.setLogger(Application.LOG);
			};

			state(MOVING).update = s -> {
				moveControl.update();
			};
		}
	}

	private final StoneMoveControl moveControl = new StoneMoveControl();

	public enum MoveState {
		GET_MOVE_START, GET_MOVE_END, MOVING
	};

	private class StoneMoveControl extends StateMachine<MoveState, String> {

		public StoneMoveControl() {
			super("Stone Move", MoveState.class, MoveState.GET_MOVE_START);

			state(MoveState.GET_MOVE_START).entry = s -> clearMove();

			state(MoveState.GET_MOVE_START).update = s -> readMoveStartPosition();

			change(MoveState.GET_MOVE_START, MoveState.GET_MOVE_END, () -> moveStartPosition != -1);

			state(MoveState.GET_MOVE_END).entry = s -> moveDirection = null;

			state(MoveState.GET_MOVE_END).update = s -> readMoveDirection();

			change(MoveState.GET_MOVE_END, MoveState.MOVING, () -> moveEndPosition != -1);

			state(MoveState.MOVING).entry = s -> computeMoveEndPosition();

			state(MoveState.MOVING).update = s -> movingStone.tf.move();

			change(MoveState.MOVING, MoveState.GET_MOVE_START, () -> isMoveEndPositionReached());

			state(MoveState.MOVING).exit = s -> {
				board.moveStone(moveStartPosition, moveEndPosition);
				changeTurn();
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
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			resetGame();
			playControl.setState(INITIALIZED);
		}
		playControl.update();
	}

	private void resetGame() {
		board = new Board(600, 600);
		board.tf.moveTo(100, 100);
		app.entities.add(board);

		startText = new ScrollingText();
		startText.setColor(Color.BLACK);
		startText.setFont(new Font("Sans", Font.PLAIN, 20));
		startText.setText("Drücke SPACE zum Start");
		startText.tf.moveTo(0, getHeight() - 40);
		app.entities.add(startText);
	}

	private void changeTurn() {
		turn = turn == WHITE ? BLACK : WHITE;
	}

	private void placeStone() {
		int gesetzt = placeStoneInteractively();
		if (gesetzt != -1) {
			StoneColor meineFarbe = turn;
			if (board.isInsideMill(gesetzt, meineFarbe)) {
				// Mühle wurde geschlossen
				mustRemoveOppositeStone = true;
			}
			changeTurn();
		}
	}

	private int placeStoneInteractively() {
		if (!mouse.clicked())
			return -1;

		int placedAt = -1;
		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p != -1 && board.getStoneAt(p) == null) {
			// An Position p Stein setzen:
			placedAt = p;
			if (turn == WHITE) {
				board.placeStoneAt(placedAt, WHITE);
				numWhiteStonesSet += 1;
			} else {
				board.placeStoneAt(placedAt, BLACK);
				numBlackStonesSet += 1;
			}
		}
		return placedAt;
	}

	private void removeStone() {
		if (!mouse.clicked())
			return;

		StoneColor color = turn;
		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return;
		}
		if (board.getStoneAt(p) == null) {
			LOG.info("Kein Stein an Klickposition");
			return;
		}
		if (board.getStoneAt(p).getColor() != color) {
			LOG.info("Stein an Klickposition besitzt die falsche Farbe");
			return;
		}
		if (board.isInsideMill(p, color) && !board.allStonesOfColorInsideMills(color)) {
			LOG.info("Stein darf nicht aus Mühle entfernt werden, weil anderer Stein außerhalb Mühle existiert");
			return;
		}
		board.removeStoneAt(p);
		mustRemoveOppositeStone = false;
	}

	private void clearMove() {
		movingStone = null;
		moveStartPosition = -1;
		moveEndPosition = -1;
		moveDirection = null;
	}

	private void readMoveStartPosition() {
		if (!mouse.clicked())
			return;

		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return;
		}
		Stone stone = board.getStoneAt(p);
		if (stone == null) {
			LOG.info("Kein Stein an Klickposition gefunden");
			return;
		}
		if (turn != stone.getColor()) {
			LOG.info(stone.getColor() + " ist nicht am Zug");
			return;
		}
		if (!board.hasEmptyNeighbor(p)) {
			LOG.info("Stein an dieser Position kann nicht ziehen");
			return;
		}
		moveStartPosition = p;
		movingStone = stone;
	}

	private void readMoveDirection() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_UP)) {
			moveDirection = NORTH;
			movingStone.tf.setVelocity(0, -speed);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_RIGHT)) {
			moveDirection = EAST;
			movingStone.tf.setVelocity(speed, 0);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_DOWN)) {
			moveDirection = SOUTH;
			movingStone.tf.setVelocity(0, speed);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_LEFT)) {
			moveDirection = WEST;
			movingStone.tf.setVelocity(-speed, 0);
		}
		if (moveDirection != null) {
			computeMoveEndPosition();
		}
	}

	private void computeMoveEndPosition() {
		moveEndPosition = -1;
		int targetPosition = board.findNeighbor(moveStartPosition, moveDirection);
		if (targetPosition != -1 && !board.isStoneAt(targetPosition)) {
			moveEndPosition = targetPosition;
		}
	}

	private boolean isMoveEndPositionReached() {
		Ellipse2D center = new Ellipse2D.Float(movingStone.tf.getX() - 2, movingStone.tf.getY() - 2, 4, 4);
		return center.contains(board.computeCenterPoint(moveEndPosition));
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(getBgColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		board.draw(g);
		drawStatus(g);
	}

	private void drawStatus(Graphics2D g) {
		if (playControl.is(INITIALIZED)) {
			startText.hCenter(getWidth());
			startText.draw(g);
			return;
		}
		if (playControl.is(PLACING)) {
			String text = format("Setzen: Weiß hat %d Stein(e) übrig, Schwarz hat %d Stein(e) übrig",
					NUM_STONES - numWhiteStonesSet, NUM_STONES - numBlackStonesSet);
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}
		if (playControl.is(MOVING)) {
			if (moveStartPosition != -1) {
				Point p = board.computeCenterPoint(moveStartPosition);
				g.setColor(Color.GREEN);
				g.fillOval(Math.round(board.tf.getX() + p.x) - 5, Math.round(board.tf.getY() + p.y) - 5, 10, 10);
			}
			if (moveEndPosition != -1) {
				Point p = board.computeCenterPoint(moveEndPosition);
				g.setColor(Color.RED);
				g.fillOval(Math.round(board.tf.getX() + p.x) - 5, Math.round(board.tf.getY() + p.y) - 5, 10, 10);
			}
			String text = (turn == WHITE ? "Weiß" : "Schwarz") + " am Zug";
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}
	}
}