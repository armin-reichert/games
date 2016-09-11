package de.amr.games.windrad.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.Timer;

import de.amr.games.windrad.model.WindparkModell;
import de.amr.games.windrad.model.Windrad;

public class WindradAnsicht {

	public final WindparkModell windpark;
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
		// Turm
		g.setColor(Color.GRAY);
		g.fill(windrad.turm);
		// Nabe
		g.setColor(selektiert ? Color.YELLOW : Color.LIGHT_GRAY);
		g.fill(windrad.nabe);
		// Rotoren
		double teilWinkel = 2 * Math.PI / windrad.anzahlRotoren();
		for (int i = 0; i < windrad.anzahlRotoren(); ++i) {
			Shape rotor = windrad.rotoren[i];
			double winkel = Math.toRadians(windrad.rotorAuslenkungGrad) + i * teilWinkel;
			AffineTransform t = new AffineTransform();
			t.translate(windrad.nabe.getCenterX(), windrad.nabe.getCenterY());
			t.rotate(winkel);
			g.setColor(i == 0 ? Color.RED : Color.LIGHT_GRAY);
			g.fill(t.createTransformedShape(rotor));
		}
	}

	public void zeichneSchatten(Graphics2D g) {
		Point2D.Float schattenStart = new Point2D.Float(windrad.basis().x + windrad.turmBreite() / 2,
				windrad.basis().y);
		Point2D.Float schattenEnde = windpark.berechneSchattenPunkt(windrad);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2));
		g.drawLine((int) schattenStart.x, (int) schattenStart.y, (int) schattenEnde.x,
				(int) schattenEnde.y);
	}
}