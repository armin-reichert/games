package de.amr.games.windrad.ui;

import static java.lang.Math.PI;
import static java.lang.Math.round;
import static java.lang.Math.toRadians;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.Timer;

import de.amr.games.windrad.model.Windpark;
import de.amr.games.windrad.model.Windrad;

public class WindradAnsicht {

	public final Windpark windpark;
	public final Windrad windrad;
	private final Timer rotorTimer;
	private int rotorDrehungInGrad = 3;

	public WindradAnsicht(WindparkAnsicht windParkAnsicht, Windrad windrad) {
		this.windrad = windrad;
		windpark = windParkAnsicht.windpark;
		rotorTimer = new Timer(0, listener -> {
			windrad.ändereRotorAuslenkung(rotorDrehungInGrad);
			windParkAnsicht.repaint();
		});
		rotorTimer.setDelay(60);
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

		g.setColor(Color.GRAY);
		g.fill(windrad.turm);

		g.setColor(Color.WHITE);
		g.fill(windrad.nabe);
		if (selektiert) {
			g.setColor(Color.RED);
			g.draw(windrad.nabe);
		}

		g.setColor(Color.LIGHT_GRAY);
		g.translate(windrad.nabe.getCenterX(), windrad.nabe.getCenterY());
		for (int i = 0, n = windrad.anzahlRotoren(); i < n; ++i) {
			Ellipse2D.Float ellipse = new Ellipse2D.Float(0, -windrad.rotorBreite / 2, windrad.rotorLänge,
					windrad.rotorBreite);
			int auslenkung = windrad.rotorAuslenkungGrad + i * 360 / n;
			g.rotate(toRadians(auslenkung));
			g.fill(ellipse);
			g.rotate(-toRadians(auslenkung));
		}
		g.translate(-windrad.nabe.getCenterX(), -windrad.nabe.getCenterY());
	}

	public void zeichneSchatten(Graphics2D g) {
		Point2D.Float schattenStart = new Point2D.Float(windrad.basis().x + windrad.getTurmBreite() / 2,
				windrad.basis().y);
		Point2D.Float schattenEnde = windpark.berechneSchattenPunkt(windrad);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2));
		g.drawLine((int) schattenStart.x, (int) schattenStart.y, (int) schattenEnde.x,
				(int) schattenEnde.y);
	}
}