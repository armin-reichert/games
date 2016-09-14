package de.amr.games.windrad.model;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Windrad (Model).
 *
 */
public class Windrad {

	private float turmBreiteUnten;
	private float turmBreiteOben;
	private float turmHöhe;
	private float nabenRadius;
	private float rotorLänge;
	private float rotorBreite;
	private float rotorWinkel;

	private final Point2D.Float position;
	private final Path2D turm;
	private final Ellipse2D.Float nabe;

	public Windrad(float baseX, float baseY, float turmHöhe, float turmBreiteUnten,
			float turmBreiteOben, float nabenRadius, float rotorLänge, float rotorBreite) {

		this.turmHöhe = turmHöhe;
		this.turmBreiteUnten = turmBreiteUnten;
		this.turmBreiteOben = turmBreiteOben;
		this.nabenRadius = nabenRadius;
		this.rotorLänge = rotorLänge;
		this.rotorBreite = rotorBreite;
		rotorWinkel = 0;
		position = new Point2D.Float(baseX, baseY);
		turm = new Path2D.Float();
		nabe = new Ellipse2D.Float(0, 0, 2 * nabenRadius, 2 * nabenRadius);
		errichte(turmHöhe);
	}

	public void errichte(float turmHöhe) {
		if (turmHöhe - rotorLänge < getMinBodenAbstand()) {
			throw new IllegalStateException("Turmhöhe ist zu niedrig: " + turmHöhe);
		}
		this.turmHöhe = turmHöhe;
		float hbo = turmBreiteOben / 2, hbu = turmBreiteUnten / 2;
		turm.reset();
		turm.moveTo(position.x - hbu, position.y); // links unten
		turm.lineTo(position.x - hbo, position.y + turmHöhe); // links oben
		turm.lineTo(position.x + hbo, position.y + turmHöhe); // rechts oben
		turm.lineTo(position.x + hbu, position.y); // rechts unten
		turm.closePath();
		nabe.x = position.x - nabenRadius;
		nabe.y = position.y + turmHöhe;
		System.out.println("Windrad errichtet, Höhe: " + turmHöhe);
	}

	public void minimiere() {
		errichte(getMinTurmHöhe());
		setzeRotorLänge(getMinRotorLänge());
		setzeNabenRadius(getMinNabenRadius());
	}

	public float getRotorBreite() {
		return rotorBreite;
	}

	public float getRotorLänge() {
		return rotorLänge;
	}

	/**
	 * @return Auslenkungswinkel des ersten Rotors in Grad.
	 *         <p>
	 *         Nullage horizontal nach rechts, Positive Richtung = Gegenuhrzeigersinn.
	 */
	public float getRotorWinkel() {
		return rotorWinkel;
	}

	public Path2D getTurm() {
		return turm;
	}

	public Ellipse2D.Float getNabe() {
		return nabe;
	}

	public float getTurmBreiteOben() {
		return turmBreiteOben;
	}

	public float getTurmBreiteUnten() {
		return turmBreiteUnten;
	}

	public float getTurmHöhe() {
		return turmHöhe;
	}

	public float getNabenRadius() {
		return nabenRadius;
	}

	public int getRotorAnzahl() {
		return 3;
	}

	/**
	 * @return Gesamthöhe incl. Rotoren.
	 */
	public float getHöhe() {
		return turmHöhe + nabenRadius * 2 + rotorLänge;
	}

	/**
	 * @return Gesamtbreite incl. Rotoren.
	 */
	public float getBreite() {
		return 2 * (nabenRadius + rotorLänge);
	}

	public Point2D.Float getPosition() {
		return position;
	}

	public void verschiebe(float x, float y) {
		position.setLocation(x, y);
		errichte(turmHöhe);
	}

	public void ändereRotorAuslenkung(float grad) {
		setzeRotorAuslenkung(rotorWinkel + grad);
	}

	public void setzeRotorAuslenkung(float grad) {
		if (grad >= 360) {
			grad = grad % 360;
		} else if (grad < 0) {
			grad = grad % 360 + 360;
		}
		rotorWinkel = grad;
	}

	public void setzeRotorLänge(float länge) {
		if (länge < getMinRotorLänge()) {
			throw new IllegalStateException("Rotorlänge zu klein: " + länge);
		}
		if (turmHöhe - länge < getMinBodenAbstand()) {
			throw new IllegalStateException("Rotorlänge zu groß: " + länge);
		}
		rotorLänge = länge;
		System.out.println("Neue Rotorlänge: " + länge);
	}

	public void setzeNabenRadius(float radius) {
		if (radius < getMinNabenRadius()) {
			throw new IllegalStateException("Nabenradius zu klein: " + radius);
		}
		if (radius > getMaxNabenRadius()) {
			throw new IllegalStateException("Nabenradius zu groß: " + radius);
		}
		nabenRadius = radius;
		nabe.width = nabe.height = 2 * nabenRadius;
		nabe.x = position.x - nabenRadius;
		nabe.y = position.y + turmHöhe;
		System.out.println("Neuer Nabenradius: " + nabenRadius);
	}

	public float getMinNabenRadius() {
		return turmBreiteOben / 2;
	}

	public float getMaxNabenRadius() {
		return turmBreiteOben;
	}

	public static float getMinRotorLänge() {
		return 20;
	}

	public static float getMinTurmHöhe() {
		return 30;
	}

	public static float getMinBodenAbstand() {
		return 10;
	}
}