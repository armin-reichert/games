package de.amr.games.muehle;

import static de.amr.easy.game.Application.LOG;
import static de.amr.games.muehle.SpielPhase.Initialisiert;
import static de.amr.games.muehle.SpielPhase.Setzen;
import static de.amr.games.muehle.SpielPhase.Spielen;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.muehle.mouse.Mouse;

public class SpielScene extends Scene<MuehleApp> {

	private final int NUM_STONES = 9;

	private final Mouse mouse;
	private final SpielSteuerung steuerung;

	private Brett brett;
	private ScrollingText startText;

	private boolean whitesTurn;
	private int whiteStonesSet;
	private int darkStonesSet;
	private boolean removeStone;
	private int startPosition;
	private int endPosition;
	private Richtung moveDirection;

	private class SpielSteuerung extends StateMachine<SpielPhase, String> {

		public SpielSteuerung() {
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
				whitesTurn = true;
				whiteStonesSet = 0;
				darkStonesSet = 0;
				removeStone = false;
			};

			state(Setzen).update = s -> {
				if (removeStone) {
					removeStone();
				} else {
					placeStone();
				}
			};

			change(Setzen, Spielen, () -> whiteStonesSet == NUM_STONES && darkStonesSet == NUM_STONES && !removeStone);

			// Spielen

			state(Spielen).entry = s -> {
				clearMove();
			};

			state(Spielen).update = s -> {
				readStartPosition();
				if (startPosition != -1) {
					readMoveDirection();
					if (moveDirection != null) {
						computeEndPosition();
						if (endPosition != -1) {
							moveStoneAnimation();
						}
					}
				}
			};
		}
	}

	public SpielScene(MuehleApp app) {
		super(app);
		setBgColor(Color.WHITE);
		mouse = new Mouse();
		app.getShell().getCanvas().addMouseListener(mouse);
		steuerung = new SpielSteuerung();
		steuerung.setLogger(Application.LOG);
	}

	private int findBoardPosition(int x, int y) {
		int brettX = Math.abs(Math.round(x - brett.tf.getX()));
		int brettY = Math.abs(Math.round(y - brett.tf.getY()));
		return brett.findNearestPosition(brettX, brettY, brett.getWidth() / 18);
	}

	@Override
	public void init() {
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
		brett = new Brett(600, 600);
		brett.tf.moveTo(100, 100);
		app.entities.add(brett);

		startText = new ScrollingText();
		startText.setColor(Color.BLACK);
		startText.setFont(new Font("Sans", Font.PLAIN, 20));
		startText.setText("Drücke SPACE zum Start");
		startText.tf.moveTo(0, getHeight() - 40);
		app.entities.add(startText);
	}

	private void placeStone() {
		int gesetzt = placeStoneInteractively();
		if (gesetzt != -1) {
			SteinFarbe meineFarbe = whitesTurn ? SteinFarbe.HELL : SteinFarbe.DUNKEL;
			if (brett.isMillPosition(gesetzt, meineFarbe)) {
				removeStone = true;
			}
			whitesTurn = !whitesTurn;
		}
	}

	private int placeStoneInteractively() {
		if (!mouse.clicked())
			return -1;

		int placedAt = -1;
		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p != -1 && brett.getStone(p) == null) {
			// An Position p Stein setzen:
			placedAt = p;
			if (whitesTurn) {
				brett.placeStone(SteinFarbe.HELL, placedAt);
				whiteStonesSet += 1;
			} else {
				brett.placeStone(SteinFarbe.DUNKEL, placedAt);
				darkStonesSet += 1;
			}
		}
		return placedAt;
	}

	private void removeStone() {
		if (!mouse.clicked())
			return;

		SteinFarbe color = whitesTurn ? SteinFarbe.HELL : SteinFarbe.DUNKEL;
		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return;
		}
		if (brett.getStone(p) == null) {
			LOG.info("Keine Stein an Klickposition");
			return;
		}
		if (brett.getStone(p).getColor() != color) {
			LOG.info("Stein an Klickposition besitzt die falsche Farbe");
			return;
		}
		if (brett.isMillPosition(p, color) && !brett.allStonesOfColorInsideMills(color)) {
			LOG.info("Stein darf nicht aus Mühle entfernt werden, weil anderer Stein außerhalb Mühle existiert");
			return;
		}
		brett.removeStone(p);
		removeStone = false;
	}

	private void readStartPosition() {
		if (!mouse.clicked())
			return;

		int p = findBoardPosition(mouse.getX(), mouse.getY());
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			return;
		}
		if (brett.getStone(p) == null) {
			LOG.info("Kein Stein an Klickposition gefunden");
			return;
		}
		if (whitesTurn && brett.getStone(p).getColor() == SteinFarbe.DUNKEL) {
			LOG.info("Schwarz ist nicht am Zug");
			return;
		}
		if (!whitesTurn && brett.getStone(p).getColor() == SteinFarbe.HELL) {
			LOG.info("Weiß ist nicht am Zug");
			return;
		}
		if (!brett.hasEmptyNeighbor(p)) {
			LOG.info("Stein an dieser Position kann nicht ziehen");
			return;
		}
		startPosition = p;
	}

	private void readMoveDirection() {
		Stein stein = brett.getStone(startPosition);
		if (Keyboard.keyPressedOnce(KeyEvent.VK_UP)) {
			moveDirection = Richtung.Norden;
			stein.tf.setVelocity(0, -1);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_RIGHT)) {
			moveDirection = Richtung.Osten;
			stein.tf.setVelocity(1, 0);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_DOWN)) {
			moveDirection = Richtung.Süden;
			stein.tf.setVelocity(0, 1);
		}
		if (Keyboard.keyPressedOnce(KeyEvent.VK_LEFT)) {
			moveDirection = Richtung.Westen;
			stein.tf.setVelocity(-1, 0);
		}
	}

	private void computeEndPosition() {
		endPosition = -1;
		int neighbor = brett.findNeighbor(startPosition, moveDirection);
		if (neighbor != -1 && brett.getStone(neighbor) == null) {
			endPosition = neighbor;
		}
	}

	private void moveStoneAnimation() {
		Stein stone = brett.getStone(startPosition);
		stone.tf.move();
		checkMoveFinished(stone);
	}

	private void checkMoveFinished(Stein stone) {
		Point endPoint = brett.computeDrawPoint(endPosition);
		if (stone.tf.getX() == endPoint.getX() && stone.tf.getY() == endPoint.getY()) {
			brett.removeStone(startPosition);
			brett.placeStone(stone.getColor(), endPosition);
			clearMove();
			whitesTurn = !whitesTurn;
		}
	}

	private void clearMove() {
		startPosition = -1;
		endPosition = -1;
		moveDirection = null;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(getBgColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		brett.draw(g);
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
					NUM_STONES - whiteStonesSet, NUM_STONES - darkStonesSet);
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}
		if (steuerung.is(Spielen)) {
			if (startPosition != -1) {
				Point p = brett.computeDrawPoint(startPosition);
				g.setColor(Color.GREEN);
				g.fillOval(Math.round(brett.tf.getX() + p.x) - 5, Math.round(brett.tf.getY() + p.y) - 5, 10, 10);
			}
			if (endPosition != -1) {
				Point p = brett.computeDrawPoint(endPosition);
				g.setColor(Color.RED);
				g.fillOval(Math.round(brett.tf.getX() + p.x) - 5, Math.round(brett.tf.getY() + p.y) - 5, 10, 10);
			}
			String text = (whitesTurn ? "Weiß" : "Schwarz") + " am Zug";
			g.setColor(Color.BLACK);
			g.setFont(new Font("Sans", Font.PLAIN, 20));
			g.drawString(text, 20, getHeight() - 20);
			return;
		}
	}
}