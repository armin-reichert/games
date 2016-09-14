package de.amr.games.windrad.ui;

import static java.lang.Math.toRadians;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.Timer;

import de.amr.games.windrad.model.Windrad;

/**
 * Windrad (View).
 */
public class WindradAnsicht {

	private final Windrad windrad;
	private float gradProSchritt;
	private Color turmFarbe;
	private Color nabenFarbe;
	private Color rotorFarbe;
	private Timer animation;

	public WindradAnsicht(WindparkAnsicht windParkAnsicht, Windrad windrad) {
		this.windrad = windrad;
		gradProSchritt = 3;
		turmFarbe = new Color(200, 200, 200);
		nabenFarbe = new Color(150, 150, 150);
		rotorFarbe = new Color(220, 220, 220);
		animation = new Timer(0, e -> {
			windrad.ändereRotorAuslenkung(gradProSchritt);
			windParkAnsicht.repaint();
		});
		animation.setDelay(60);
	}

	public Windrad getWindrad() {
		return windrad;
	}

	public void ändereDrehrichtung() {
		gradProSchritt = -gradProSchritt;
	}

	public boolean drehtSich() {
		return animation.isRunning();
	}

	public void start() {
		animation.restart();
	}

	public void stop() {
		animation.stop();
	}

	public void draw(Graphics2D g, boolean selektiert) {
		g.setColor(turmFarbe);
		g.fill(windrad.getTurm());

		g.setColor(nabenFarbe);
		g.fill(windrad.getNabe());

		Ellipse2D.Float rotor = new Ellipse2D.Float(0, -windrad.getRotorBreite() / 2,
				windrad.getRotorLänge(), windrad.getRotorBreite());
		g.setColor(selektiert ? Color.YELLOW : rotorFarbe);
		AffineTransform ot = g.getTransform();
		for (int i = 0, n = windrad.getRotorAnzahl(); i < n; ++i) {
			AffineTransform t = new AffineTransform();
			t.translate(windrad.getNabe().getCenterX(), windrad.getNabe().getCenterY());
			t.rotate(toRadians(windrad.getRotorWinkel() + i * 360 / n));
			g.transform(t);
			g.fill(rotor);
			g.setTransform(ot);
		}
	}
}