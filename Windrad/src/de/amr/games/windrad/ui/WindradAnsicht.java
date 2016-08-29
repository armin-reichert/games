package de.amr.games.windrad.ui;

import static de.amr.games.windrad.model.WindradModell.ANZAHL_ROTOREN;
import static java.lang.Math.round;
import static java.lang.Math.toRadians;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import javax.swing.Timer;

import de.amr.games.windrad.model.WindparkModell;
import de.amr.games.windrad.model.WindradModell;

public class WindradAnsicht {

	public final WindparkModell windpark;
	public final WindradModell windrad;
	private final Timer rotorTimer;
	private int rotorDrehungInGrad = 3;

	public WindradAnsicht(WindparkAnsicht windParkAnsicht, WindradModell windrad) {
		this.windrad = windrad;
		windpark = windParkAnsicht.windpark;
		rotorTimer = new Timer(0, listener -> {
			windrad.ändereRotorAuslenkung(rotorDrehungInGrad);
			windParkAnsicht.repaint();
		});
		rotorTimer.setDelay(20);
	}

	public void ändereDrehrichtung() {
		rotorDrehungInGrad = -rotorDrehungInGrad;
	}

	public boolean inBewegung() {
		return rotorTimer.isRunning();
	}

	public void start() {
		rotorTimer.restart();
	}

	public void stop() {
		rotorTimer.stop();
	}

	public void zeichne(Graphics2D g, boolean selektiert) {
		zeichneTurm(g);
		zeichneNabe(g, selektiert);
		zeichneRotoren(g);
	}

	public void zeichneSchatten(Graphics2D g) {
		Point2D.Float schattenStart = windrad.turmRU;
		Point2D.Float schattenEnde = windpark.berechneSchattenPunkt(windrad);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2));
		g.drawLine((int) schattenStart.x, (int) schattenStart.y, (int) schattenEnde.x,
				(int) schattenEnde.y);
	}

	private void zeichneTurm(Graphics2D g) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.setColor(Color.GRAY);
		int breite = (int) windrad.turmRO.distance(windrad.turmLO);
		int höhe = (int) windrad.turmRU.distance(windrad.turmRO);
		gg.fillRect((int) windrad.turmLU.getX(), (int) windrad.turmLU.getY(), breite, höhe);
		gg.dispose();
	}

	private void zeichneNabe(Graphics2D g, boolean selektiert) {
		Graphics2D gg = (Graphics2D) g.create();
		gg.setColor(Color.LIGHT_GRAY);
		int x = (int) (windrad.nabeZentrum.getX() - windrad.nabeRadius);
		int y = (int) (windrad.nabeZentrum.getY() - windrad.nabeRadius);
		int diameter = 2 * (int) windrad.nabeRadius;
		gg.fillOval(x, y, diameter, diameter);
		if (selektiert) {
			gg.setColor(Color.YELLOW);
			gg.fillOval(x, y, diameter, diameter);
		}
		// gg.translate((int) windrad.nabeZentrum.getX(), (int) windrad.nabeZentrum.getY());
		// gg.scale(1, -1);
		// gg.setColor(Color.BLACK);
		// gg.setFont(new Font("Monospaced", Font.BOLD, (int) (windrad.nabeRadius * 0.8)));
		// String text = String.format("%03d", windrad.rotorAuslenkung);
		// int textWidth = g.getFontMetrics().stringWidth(text);
		// gg.drawString(text, -textWidth / 2 - 5, 0);
		// gg.scale(1, -1);
		gg.dispose();
	}

	private void zeichneRotoren(Graphics2D g) {
		for (int i = 0; i < ANZAHL_ROTOREN; ++i) {
			Graphics2D gg = (Graphics2D) g.create();
			gg.setColor(i == 0 ? Color.RED : Color.LIGHT_GRAY);
			Point2D rotorZentrum = windrad.rotorZentren[i];
			gg.rotate(toRadians(windrad.rotorAuslenkung + i * 360 / ANZAHL_ROTOREN), rotorZentrum.getX(),
					rotorZentrum.getY());
			int x = (int) round(rotorZentrum.getX() - windrad.rotorLänge / 2);
			int y = (int) round(rotorZentrum.getY() - windrad.rotorBreite / 2);
			gg.fillOval(x, y, (int) windrad.rotorLänge, (int) windrad.rotorBreite);
			gg.dispose();
		}
	}
}