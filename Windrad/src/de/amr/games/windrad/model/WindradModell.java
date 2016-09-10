package de.amr.games.windrad.model;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class WindradModell {

	public static final int ANZAHL_ROTOREN = 3;

	public static final float MIN_BODEN_ABSTAND = 10;
	public static final float MIN_TURM_HÖHE = 30;
	public static final float MIN_ROTOR_LÄNGE = 20;

	// Turm
	public final Path2D turm = new Path2D.Float();
	private float turmBaseX, turmBaseY; // unten Mitte
	private float turmBreite;
	private float turmHöhe;

	// Nabe (Kreis)
	public final Point2D.Float nabeZentrum = new Point2D.Float();
	public float nabeRadius;

	// Rotoren (Ellipsen)
	public final Point2D.Float[] rotorZentren = new Point2D.Float[3];
	public float rotorLänge;
	public float rotorBreite;
	public int rotorAuslenkung; // Auslenkung in Grad des ersten Rotors

	public WindradModell(float turmBaseX, float turmBaseY, float turmHöhe, float turmBreite,
			float nabeRadius, float rotorLänge, float rotorBreite) {
		for (int i = 0; i < ANZAHL_ROTOREN; ++i) {
			rotorZentren[i] = new Point2D.Float();
		}
		this.nabeRadius = nabeRadius;
		this.rotorLänge = rotorLänge;
		this.rotorBreite = rotorBreite;
		rotorAuslenkung = 90;

		this.turmBaseX = turmBaseX;
		this.turmBaseY = turmBaseY;
		this.turmBreite = turmBreite;
		this.turmHöhe = turmHöhe;

		errichteWindrad(turmHöhe);
	}

	public void errichteWindrad(float turmHöhe) {
		if (turmHöhe - rotorLänge < MIN_BODEN_ABSTAND) {
			throw new IllegalStateException("Turmhöhe ist zu niedrig: " + turmHöhe);
		}

		// Turm
		this.turmHöhe = turmHöhe;
		float radius = turmBreite / 2;
		turm.moveTo(turmBaseX - radius, turmBaseY); // links unten
		turm.lineTo(turmBaseX - radius, turmBaseY + turmHöhe); // links oben
		turm.lineTo(turmBaseX + radius, turmBaseY + turmHöhe); // rechts oben
		turm.lineTo(turmBaseX + radius, turmBaseY); // rechts unten
		turm.closePath();

		// Nabe und Rotoren
		nabeZentrum.setLocation(turmBaseX, turmBaseY + turmHöhe + nabeRadius);
		aktualisiereRotoren();

		System.out.println("Windrad errichtet, Höhe: " + turmHöhe);
	}

	public float turmHöhe() {
		return turmHöhe;
	}

	public float turmBreite() {
		return turmBreite;
	}

	public float gesamtHöhe() {
		return turmHöhe + nabeRadius * 2 + rotorLänge;
	}

	public float gesamtBreite() {
		return 2 * (nabeRadius + rotorLänge);
	}

	public Point2D.Float basis() {
		return new Point2D.Float(turmBaseX, turmBaseY);
	}

	public void verschiebe(float x, float y) {
		turmBaseX = x;
		turmBaseY = y;
		errichteWindrad(turmHöhe);
	}

	public void setzeRotorAuslenkung(int grad) {
		if (grad >= 360) {
			grad = grad % 360;
		} else if (grad < 0) {
			grad = grad % 360 + 360;
		}
		rotorAuslenkung = grad;
		aktualisiereRotoren();
	}

	public void ändereRotorAuslenkung(int grad) {
		setzeRotorAuslenkung(rotorAuslenkung + grad);
	}

	public void aktualisiereRotoren() {
		double rotorAuslenkungRad = Math.toRadians(rotorAuslenkung);
		double teilWinkel = 2 * Math.PI / ANZAHL_ROTOREN;
		for (int i = 0; i < ANZAHL_ROTOREN; ++i) {
			double winkel = rotorAuslenkungRad + i * teilWinkel;
			double radius = (nabeRadius + rotorLänge / 2);
			double x = nabeZentrum.getX() + radius * Math.cos(winkel);
			double y = nabeZentrum.getY() + radius * Math.sin(winkel);
			rotorZentren[i].setLocation(x, y);
		}
	}

	public void setzeRotorLänge(float länge) {
		if (länge < MIN_ROTOR_LÄNGE) {
			throw new IllegalStateException("Rotorlänge zu klein: " + länge);
		}
		if (turmHöhe() - länge < MIN_BODEN_ABSTAND) {
			throw new IllegalStateException("Rotorlänge zu groß: " + länge);
		}
		rotorLänge = länge;
		aktualisiereRotoren();
		System.out.println("Neue Rotorlänge: " + länge);
	}

	public void setzeNabeRadius(float radius) {
		if (2 * radius < turmBreite()) {
			throw new IllegalStateException("Nabenradius zu klein: " + radius);
		}
		if (radius > 2 * turmBreite()) {
			throw new IllegalStateException("Nabenradius zu groß: " + radius);
		}
		nabeRadius = radius;
		nabeZentrum.y = turmBaseY + turmHöhe + radius;
		aktualisiereRotoren();
		System.out.println("Neuer Nabenradius: " + radius);
	}

	public void minimieren() {
		setzeRotorLänge(MIN_ROTOR_LÄNGE);
		errichteWindrad(MIN_TURM_HÖHE);
		setzeNabeRadius(turmBreite() / 2);
	}
}
