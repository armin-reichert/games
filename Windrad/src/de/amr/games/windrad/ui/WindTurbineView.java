package de.amr.games.windrad.ui;

import static java.lang.Math.toRadians;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.Timer;

import de.amr.games.windrad.model.WindTurbine;

/**
 * Windrad (View).
 */
public class WindTurbineView {

	private final WindTurbine turbine;
	private float degreesPerStep;
	private Color towerColor;
	private Color nacelleColor;
	private Color rotorColor;
	private Timer animation;

	public WindTurbineView(WindFarmView farmView, WindTurbine turbine) {
		this.turbine = turbine;
		degreesPerStep = 3;
		towerColor = new Color(200, 200, 200);
		nacelleColor = new Color(150, 150, 150);
		rotorColor = new Color(220, 220, 220);
		animation = new Timer(0, e -> {
			turbine.changeRotorPosition(degreesPerStep);
			farmView.repaint();
		});
		animation.setDelay(60);
	}

	public WindTurbine getTurbine() {
		return turbine;
	}

	public void changeDirection() {
		degreesPerStep = -degreesPerStep;
	}

	public boolean isRunning() {
		return animation.isRunning();
	}

	public void start() {
		animation.restart();
	}

	public void stop() {
		animation.stop();
	}

	public void draw(Graphics2D g, boolean selected) {
		g.setColor(towerColor);
		g.fill(turbine.getTower());

		g.setColor(nacelleColor);
		g.fill(turbine.getNacelle());

		Ellipse2D.Float rotor = new Ellipse2D.Float(0, -turbine.getRotorThickness() / 2, turbine.getRotorLength(),
				turbine.getRotorThickness());
		g.setColor(selected ? Color.YELLOW : rotorColor);
		AffineTransform ot = g.getTransform();
		for (int i = 0, n = turbine.getRotorCount(); i < n; ++i) {
			AffineTransform t = new AffineTransform();
			t.translate(turbine.getNacelle().getCenterX(), turbine.getNacelle().getCenterY());
			t.rotate(toRadians(turbine.getRotorPosition() + i * 360 / n));
			g.transform(t);
			g.fill(rotor);
			g.setTransform(ot);
		}
	}
}