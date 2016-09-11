package de.amr.games.windrad.model;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Windrad als Modellelement.
 *
 */
public class Windrad {

	public static final float MIN_BODEN_ABSTAND = 10;
	public static final float MIN_TURM_HÖHE = 30;
	public static final float MIN_ROTOR_LÄNGE = 20;

	// Aufstellpunkt
	private final Point2D.Float basis;

	// Turm
	private float turmBreite;
	private float turmHöhe;

	// Nabe
	public float nabeRadius;

	// Rotoren
	private final int anzahlRotoren;
	public float rotorLänge;
	public float rotorBreite;
	public int rotorAuslenkungGrad; // Auslenkung des ersten Rotors in Grad

	// Shapes
	public final Path2D turm;
	public final Ellipse2D.Float nabe;
	// public final Point2D.Float[] rotorZentren;
	public final Ellipse2D.Float[] rotoren;

	public Windrad(float baseX, float baseY, float turmHöhe, float turmBreite, float nabeRadius,
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
		nabe = new Ellipse2D.Float(0, 0, 2 * nabeRadius, 2 * nabeRadius);
		rotoren = new Ellipse2D.Float[anzahlRotoren];
		for (int i = 0; i < anzahlRotoren; ++i) {
			rotoren[i] = new Ellipse2D.Float(0, 0, rotorLänge, rotorBreite);
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
		nabe.x = basis.x - nabeRadius;
		nabe.y = basis.y + turmHöhe;
		aktualisiereRotoren();

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

	public void ändereRotorAuslenkung(int grad) {
		setzeRotorAuslenkung(rotorAuslenkungGrad + grad);
	}

	public void setzeRotorAuslenkung(int grad) {
		if (grad >= 360) {
			grad = grad % 360;
		} else if (grad < 0) {
			grad = grad % 360 + 360;
		}
		rotorAuslenkungGrad = grad;
		aktualisiereRotoren();
	}

	private void aktualisiereRotoren() {
		for (Ellipse2D.Float rotor : rotoren) {
			rotor.x = 0;
			rotor.y = 0;
			rotor.width = rotorLänge;
			rotor.height = rotorBreite;
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
		nabe.width = nabe.height = 2 * nabeRadius;
		nabe.x = basis.x - nabeRadius;
		nabe.y = basis.y + turmHöhe;
		aktualisiereRotoren();
		System.out.println("Neuer Nabenradius: " + nabeRadius);
	}

	public void minimieren() {
		setzeRotorLänge(MIN_ROTOR_LÄNGE);
		aufstellen(MIN_TURM_HÖHE);
		setzeNabeRadius(turmBreite() / 2);
	}
}
