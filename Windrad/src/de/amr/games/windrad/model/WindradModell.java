package de.amr.games.windrad.model;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class WindradModell {

	public static final float MIN_BODEN_ABSTAND = 10;
	public static final float MIN_TURM_HÖHE = 30;
	public static final float MIN_ROTOR_LÄNGE = 20;

	// Aufstellpunkt
	private final Point2D.Float basis;

	// Turm
	public final Path2D turm;
	private float turmBreite;
	private float turmHöhe;

	// Nabe
	public final Point2D.Float nabeZentrum;
	public float nabeRadius;

	// Rotoren
	private final int anzahlRotoren;
	public final Point2D.Float[] rotorZentren;
	public float rotorLänge;
	public float rotorBreite;
	public int rotorAuslenkungGrad; // Auslenkung des ersten Rotors in Grad

	public WindradModell(float baseX, float baseY, float turmHöhe, float turmBreite, float nabeRadius,
			float rotorLänge, float rotorBreite) {

		this.turmHöhe = turmHöhe;
		this.turmBreite = turmBreite;
		this.nabeRadius = nabeRadius;
		this.rotorLänge = rotorLänge;
		this.rotorBreite = rotorBreite;
		anzahlRotoren = 3;
		rotorAuslenkungGrad = 90;

		basis = new Point2D.Float(baseX, baseY);
		turm = new Path2D.Float();
		nabeZentrum = new Point2D.Float();
		rotorZentren = new Point2D.Float[anzahlRotoren];
		for (int i = 0; i < anzahlRotoren; ++i) {
			rotorZentren[i] = new Point2D.Float();
		}
		aufstellen(turmHöhe);
	}

	public void aufstellen(float turmHöhe) {
		if (turmHöhe - rotorLänge < MIN_BODEN_ABSTAND) {
			throw new IllegalStateException("Turmhöhe ist zu niedrig: " + turmHöhe);
		}

		// Turm aktualisieren
		this.turmHöhe = turmHöhe;
		float hb = turmBreite / 2;
		turm.reset();
		turm.moveTo(basis.x - hb, basis.y); // links unten
		turm.lineTo(basis.x - hb, basis.y + turmHöhe); // links oben
		turm.lineTo(basis.x + hb, basis.y + turmHöhe); // rechts oben
		turm.lineTo(basis.x + hb, basis.y); // rechts unten
		turm.closePath();

		// Nabe und Rotorpositionen aktualisieren
		nabeZentrum.setLocation(basis.x, basis.y + turmHöhe + nabeRadius);
		aktualisiereRotorPositionen();

		System.out.println("Windrad errichtet, Höhe: " + turmHöhe);
	}

	public int anzahlRotoren() {
		return anzahlRotoren;
	}
	
	public float turmHöhe() {
		return turmHöhe;
	}

	public float turmBreite() {
		return turmBreite;
	}

	public float höhe() {
		return turmHöhe + nabeRadius * 2 + rotorLänge;
	}

	public float breite() {
		return 2 * (nabeRadius + rotorLänge);
	}

	public Point2D.Float basis() {
		return basis;
	}

	public void verschiebe(float x, float y) {
		basis.setLocation(x, y);
		aufstellen(turmHöhe);
	}

	public void setzeRotorAuslenkung(int grad) {
		if (grad >= 360) {
			grad = grad % 360;
		} else if (grad < 0) {
			grad = grad % 360 + 360;
		}
		rotorAuslenkungGrad = grad;
		aktualisiereRotorPositionen();
	}

	public void ändereRotorAuslenkung(int grad) {
		setzeRotorAuslenkung(rotorAuslenkungGrad + grad);
	}

	public void aktualisiereRotorPositionen() {
		double rotorAuslenkungRadians = Math.toRadians(rotorAuslenkungGrad);
		double teilWinkel = 2 * Math.PI / anzahlRotoren;
		for (int i = 0; i < anzahlRotoren; ++i) {
			double winkel = rotorAuslenkungRadians + i * teilWinkel;
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
		aktualisiereRotorPositionen();
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
		nabeZentrum.y = basis.y + turmHöhe + radius;
		aktualisiereRotorPositionen();
		System.out.println("Neuer Nabenradius: " + radius);
	}

	public void minimieren() {
		setzeRotorLänge(MIN_ROTOR_LÄNGE);
		aufstellen(MIN_TURM_HÖHE);
		setzeNabeRadius(turmBreite() / 2);
	}
}
