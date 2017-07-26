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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.ScrollingText;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;

public class SpielScene extends Scene<MuehleApp> {

	private final int STEINE = 9;

	private MouseEvent mouseClick;

	private SpielSteuerung steuerung;
	private Brett brett;
	private ScrollingText startText;

	private boolean weißAmZug;
	private int weißeSteineGesetzt;
	private int schwarzeSteineGesetzt;
	private boolean gegnerSteinEntfernen;
	private int startPosition;
	private int endPosition;
	private Richtung bewegungsRichtung;

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
				weißAmZug = true;
				weißeSteineGesetzt = 0;
				schwarzeSteineGesetzt = 0;
				gegnerSteinEntfernen = false;
				mouseClick = null;
			};

			state(Setzen).update = s -> {
				if (gegnerSteinEntfernen) {
					steinMitMausEntfernen(weißAmZug ? SteinFarbe.HELL : SteinFarbe.DUNKEL);
				} else {
					int gesetzt = steinMitMausSetzen();
					if (gesetzt != -1) {
						SteinFarbe meineFarbe = weißAmZug ? SteinFarbe.HELL : SteinFarbe.DUNKEL;
						if (brett.insideMill(gesetzt, meineFarbe)) {
							gegnerSteinEntfernen = true;
						}
						weißAmZug = !weißAmZug;
					}
				}

			};

			change(Setzen, Spielen, () -> weißeSteineGesetzt == STEINE && schwarzeSteineGesetzt == STEINE);

			// Spielen

			state(Spielen).entry = s -> {
				startPosition = -1;
				endPosition = -1;
			};

			state(Spielen).update = s -> {
				startPositionBestimmen();
				if (startPosition != -1) {
					if (Keyboard.keyPressedOnce(KeyEvent.VK_DOWN)) {
						bewegungsRichtung = Richtung.Süden;
					}
					if (Keyboard.keyPressedOnce(KeyEvent.VK_UP)) {
						bewegungsRichtung = Richtung.Norden;
					}
					if (Keyboard.keyPressedOnce(KeyEvent.VK_LEFT)) {
						bewegungsRichtung = Richtung.Westen;
					}
					if (Keyboard.keyPressedOnce(KeyEvent.VK_RIGHT)) {
						bewegungsRichtung = Richtung.Osten;
					}
					if (bewegungsRichtung != null) {
						endPosition = brett.findNeighbor(startPosition, bewegungsRichtung);
						if (endPosition == -1 || brett.getStone(endPosition) != null) {
							endPosition = -1;
						} else {
							steinBewegen();
						}
					}
				}
			};
		}
	}

	public SpielScene(MuehleApp app) {
		super(app);
		setBgColor(Color.WHITE);
		steuerung = new SpielSteuerung();
		steuerung.setLogger(Application.LOG);

		app.getShell().getCanvas().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				mouseClick = e;
			}
		});
	}

	private int findeBrettPosition(MouseEvent e) {
		int mouseX = e.getX();
		int mouseY = e.getY();
		int brettX = Math.abs(Math.round(mouseX - brett.tf.getX()));
		int brettY = Math.abs(Math.round(mouseY - brett.tf.getY()));
		return brett.findNearestPosition(brettX, brettY, brett.getWidth() / 18);
	}

	@Override
	public void init() {
		steuerung.init();
	}

	@Override
	public void update() {
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

	@Override
	public void draw(Graphics2D g) {
		g.setColor(getBgColor());
		g.fillRect(0, 0, getWidth(), getHeight());
		brett.draw(g);

		g.setFont(new Font("Sans", Font.PLAIN, 20));
		g.setColor(Color.BLACK);
		String text = "";
		if (steuerung.is(Initialisiert)) {
			startText.hCenter(getWidth());
			startText.draw(g);
		} else if (steuerung.is(Setzen)) {
			text = format("Setzen: Weiß hat %d Stein(e) übrig, Schwarz hat %d Stein(e) übrig", STEINE - weißeSteineGesetzt,
					STEINE - schwarzeSteineGesetzt);

		} else if (steuerung.is(Spielen)) {
			if (startPosition != -1) {
				Point p = brett.getDrawPosition(startPosition);
				g.setColor(Color.GREEN);
				g.fillOval(Math.round(brett.tf.getX() + p.x) - 5, Math.round(brett.tf.getY() + p.y) - 5, 10, 10);
			}
			if (endPosition != -1) {
				Point p = brett.getDrawPosition(endPosition);
				g.setColor(Color.RED);
				g.fillOval(Math.round(brett.tf.getX() + p.x) - 5, Math.round(brett.tf.getY() + p.y) - 5, 10, 10);
			}
			text = (weißAmZug ? "Weiß" : "Schwarz") + " am Zug";
		} else {
			text = steuerung.stateID().name();
		}
		g.drawString(text, 20, getHeight() - 20);

	}

	private int steinMitMausSetzen() {
		int setzPosition = -1;
		if (mouseClick != null) {
			int p = findeBrettPosition(mouseClick);
			if (p != -1 && brett.getStone(p) == null) {
				// An Position p Stein setzen:
				setzPosition = p;
				if (weißAmZug) {
					brett.placeStone(SteinFarbe.HELL, setzPosition);
					weißeSteineGesetzt += 1;
				} else {
					brett.placeStone(SteinFarbe.DUNKEL, setzPosition);
					schwarzeSteineGesetzt += 1;
				}
			}
			mouseClick = null;
		}
		return setzPosition;
	}

	private void steinMitMausEntfernen(SteinFarbe farbe) {
		if (mouseClick == null)
			return;

		int p = findeBrettPosition(mouseClick);
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
		} else if (brett.getStone(p) != null && brett.getStone(p).getColor() == farbe) {
			if (!brett.insideMill(p, farbe) || alleSteineInMühle(farbe)) {
				brett.removeStone(p);
				gegnerSteinEntfernen = false;
			} else {
				LOG.info("Stein in Mühle darf nicht entfernt werden");
			}
		}
		mouseClick = null;
	}

	private boolean alleSteineInMühle(SteinFarbe farbe) {
		for (int p = 0; p < Brett.NUM_POS; p += 1) {
			if (brett.getStone(p) != null && brett.getStone(p).getColor() == farbe && !brett.insideMill(p, farbe)) {
				return false;
			}
		}
		return true;
	}

	private void startPositionBestimmen() {
		if (mouseClick == null)
			return;

		int p = findeBrettPosition(mouseClick);
		if (p == -1) {
			LOG.info("Keine Brettposition zu Klickposition gefunden");
			mouseClick = null;
			return;
		}
		if (brett.getStone(p) == null) {
			LOG.info("Kein Stein an Klickposition gefunden");
			mouseClick = null;
			return;
		}
		if (weißAmZug && brett.getStone(p).getColor() == SteinFarbe.DUNKEL) {
			LOG.info("Schwarz ist nicht am Zug");
			mouseClick = null;
			return;
		}
		if (!weißAmZug && brett.getStone(p).getColor() == SteinFarbe.HELL) {
			LOG.info("Weiß ist nicht am Zug");
			mouseClick = null;
			return;
		}
		startPosition = p;
		mouseClick = null;
	}

	private void steinBewegen() {
		Stein stein = brett.getStone(startPosition);
		stein.tf.setVelocity(0, 0);
		switch (bewegungsRichtung) {
		case Norden:
			stein.tf.setVelocity(0, -1);
			break;
		case Osten:
			stein.tf.setVelocity(1, 0);
			break;
		case Süden:
			stein.tf.setVelocity(0, 1);
			break;
		case Westen:
			stein.tf.setVelocity(-1, 0);
			break;
		}
		stein.tf.move();
		Point endPunkt = brett.getDrawPosition(endPosition);
		if (stein.tf.getX() == endPunkt.getX() && stein.tf.getY() == endPunkt.getY()) {
			brett.removeStone(startPosition);
			brett.placeStone(stein.getColor(), endPosition);
			startPosition = -1;
			endPosition = -1;
			bewegungsRichtung = null;
			weißAmZug = !weißAmZug;
		}
	}
}