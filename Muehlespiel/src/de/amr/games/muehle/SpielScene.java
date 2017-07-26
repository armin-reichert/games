package de.amr.games.muehle;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.Richtung.Norden;
import static de.amr.games.muehle.Richtung.Osten;
import static de.amr.games.muehle.Richtung.Süden;
import static de.amr.games.muehle.Richtung.Westen;
import static de.amr.games.muehle.SpielPhase.Initialisiert;
import static de.amr.games.muehle.SpielPhase.Setzen;
import static de.amr.games.muehle.SpielPhase.Spielen;
import static de.amr.games.muehle.SteinFarbe.BLACK;
import static de.amr.games.muehle.SteinFarbe.WHITE;
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

public class SpielScene extends Scene<MuehleApp> {

	private final int NUM_STONES = 6;

	private final Mouse mouse;

	private Brett board;
	private ScrollingText startText;

	private SteinFarbe turn;
	private int numWhiteStonesSet;
	private int numBlackStonesSet;
	private boolean mustRemoveOppositeStone;

	// stone movement
	private Stein movingStone;
	private int moveStartPosition;
	private int moveEndPosition;
	private Richtung moveDirection;

	private float speed = 3;

	private final SpielAblaufSteuerung steuerung = new SpielAblaufSteuerung();

	private class SpielAblaufSteuerung extends StateMachine<SpielPhase, String> {

		public SpielAblaufSteuerung() {
			super("Mühlespiel Steuerung", SpielPhase.class, SpielPhase.Initialisiert);

			// Initialisiert

			state(Initialisiert).entry = s -> {
				resetGame();
			};

			state(Initialisiert).exit = s -> {
				startText.visibility = () -> false;
			};

			change(Initialisiert, Setzen, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// Setzen

			state(Setzen).entry = s -> {
				turn = WHITE;
				numWhiteStonesSet = 0;
				numBlackStonesSet = 0;
				mustRemoveOppositeStone = false;
			};

			state(Setzen).update = s -> {
				if (mustRemoveOppositeStone) {
					removeStone();
				} else {
					placeStone();
				}
			};

			change(Setzen, Spielen,
					() -> numWhiteStonesSet == NUM_STONES && numBlackStonesSet == NUM_STONES && !mustRemoveOppositeStone);

			// Spielen

			state(Spielen).entry = s -> {
				clearMove();
				moveControl.init();
				moveControl.setLogger(Application.LOG);
			};

			state(Spielen).update = s -> {
				moveControl.update();
			};
		}
	}

	private final StoneMoveControl moveControl = new StoneMoveControl();

	public enum MoveState {
		Ready, ReadStartPosition, ReadDirection, Moving
	};

	private class StoneMoveControl extends StateMachine<MoveState, String> {

		public StoneMoveControl() {
			super("Stone Move", MoveState.class, MoveState.Ready);

			state(MoveState.Ready).entry = s -> clearMove();

			change(MoveState.Ready, MoveState.ReadStartPosition);

			state(MoveState.ReadStartPosition).update = s -> readMoveStartPosition();

			change(MoveState.ReadStartPosition, MoveState.ReadDirection, () -> moveStartPosition != -1);

			state(MoveState.ReadDirection).entry = s -> moveDirection = null;

			state(MoveState.ReadDirection).update = s -> readMoveDirection();

			change(MoveState.ReadDirection, MoveState.Moving, () -> moveEndPosition != -1);

			state(MoveState.Moving).entry = s -> computeMoveEndPosition();

			state(MoveState.Moving).update = s -> movingStone.tf.move();

			change(MoveState.Moving, MoveState.Ready, () -> isMoveEndPositionReached());

			state(MoveState.Moving).exit = s -> {
				board.moveStone(moveStartPosition, moveEndPosition);
				changeTurn();
			};
		}
	}

	public SpielScene(MuehleApp app) {
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
		steuerung.setLogger(Application.LOG);
		steuerung.init();
	}

	@Override
	public void update() {
		mouse.poll();
		if (Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			resetGame();
			steuerung.setState(Initialisiert);
		}
		steuerung.update();
	}

	private void resetGame() {
		board = new Brett(600, 600);
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
			SteinFarbe meineFarbe = turn;
			if (board.isMillPosition(gesetzt, meineFarbe)) {
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
		if (p != -1 && board.getStone(p) == null) {
			// An Position p Stein setzen:
			placedAt = p;
			if (turn == WHITE) {
				board.placeStone(WHITE, placedAt);
				numWhiteStonesSet += 1;
			} else {
				board.placeStone(BLACK, placedAt);
				numBlackStonesSet += 1;
			}
		}
		return placedAt;
	}

	private void removeStone() {
		if (!mouse.clicked())
			return;

		SteinFarbe color = turn;
		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return;
		}
		if (board.getStone(p) == null) {
			LOG.info("Kein Stein an Klickposition");
			return;
		}
		if (board.getStone(p).getColor() != color) {
			LOG.info("Stein an Klickposition besitzt die falsche Farbe");
			return;
		}
		if (board.isMillPosition(p, color) && !board.allStonesOfColorInsideMills(color)) {
			LOG.info("Stein darf nicht aus Mühle entfernt werden, weil anderer Stein außerhalb Mühle existiert");
			return;
		}
		board.removeStone(p);
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
		Stein stone = board.getStone(p);
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
			moveDirection = Norden;
			movingStone.tf.setVelocity(0, -speed);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_RIGHT)) {
			moveDirection = Osten;
			movingStone.tf.setVelocity(speed, 0);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_DOWN)) {
			moveDirection = Süden;
			movingStone.tf.setVelocity(0, speed);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_LEFT)) {
			moveDirection = Westen;
			movingStone.tf.setVelocity(-speed, 0);
		}
		if (moveDirection != null) {
			computeMoveEndPosition();
		}
	}

	private void computeMoveEndPosition() {
		moveEndPosition = -1;
		int targetPosition = board.findNeighbor(moveStartPosition, moveDirection);
		if (targetPosition != -1 && !board.hasStone(targetPosition)) {
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
		if (steuerung.is(Initialisiert)) {
			startText.hCenter(getWidth());
			startText.draw(g);
			return;
		}
		if (steuerung.is(Setzen)) {
			String text = format("Setzen: Weiß hat %d Stein(e) übrig, Schwarz hat %d Stein(e) übrig",
					NUM_STONES - numWhiteStonesSet, NUM_STONES - numBlackStonesSet);
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}
		if (steuerung.is(Spielen)) {
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