package de.amr.games.windrad.model;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Windrad (Model).
 *
 */
public class WindTurbine {

	private float towerWidthBottom;
	private float towerWidthTop;
	private float towerHeight;
	private float nacelleRadius;
	private float rotorLength;
	private float rotorThickness;
	private float rotorPosition;

	private final Point2D.Float position;
	private final Path2D tower;
	private final Ellipse2D.Float nacelle;

	public WindTurbine(float baseX, float baseY, float towerHeight, float towerWidthBottom,
			float towerWidthTop, float nacelleRadius, float rotorLength, float rotorThickness) {

		this.towerHeight = towerHeight;
		this.towerWidthBottom = towerWidthBottom;
		this.towerWidthTop = towerWidthTop;
		this.nacelleRadius = nacelleRadius;
		this.rotorLength = rotorLength;
		this.rotorThickness = rotorThickness;
		rotorPosition = 0;
		position = new Point2D.Float(baseX, baseY);
		tower = new Path2D.Float();
		nacelle = new Ellipse2D.Float(0, 0, 2 * nacelleRadius, 2 * nacelleRadius);
		erect(towerHeight);
	}

	public void erect(float towerHeight) {
		if (towerHeight - rotorLength < getMinBottomDistance()) {
			throw new IllegalStateException("Turmhöhe ist zu niedrig: " + towerHeight);
		}
		this.towerHeight = towerHeight;
		float hbo = towerWidthTop / 2, hbu = towerWidthBottom / 2;
		tower.reset();
		tower.moveTo(position.x - hbu, position.y); // links unten
		tower.lineTo(position.x - hbo, position.y + towerHeight); // links oben
		tower.lineTo(position.x + hbo, position.y + towerHeight); // rechts oben
		tower.lineTo(position.x + hbu, position.y); // rechts unten
		tower.closePath();
		nacelle.x = position.x - nacelleRadius;
		nacelle.y = position.y + towerHeight;
		System.out.println("Windrad errichtet, Höhe: " + towerHeight);
	}

	public void minimize() {
		rotorLength = getMinRotorLength();
		erect(getMinTowerHeight());
		setNacelleRadius(getMinNacelleRadius());
	}

	public float getRotorThickness() {
		return rotorThickness;
	}

	public float getRotorLength() {
		return rotorLength;
	}

	/**
	 * Auslenkungswinkel des ersten Rotors in Grad.
	 * <p>
	 * Nullage horizontal nach rechts, Positive Richtung = Gegenuhrzeigersinn.
	 */
	public float getRotorPosition() {
		return rotorPosition;
	}

	public Path2D getTower() {
		return tower;
	}

	public Ellipse2D.Float getNacelle() {
		return nacelle;
	}

	public float getTowerWidthTop() {
		return towerWidthTop;
	}

	public float getTowerWidthBottom() {
		return towerWidthBottom;
	}

	public float getTowerHeight() {
		return towerHeight;
	}

	public float getNacelleRadius() {
		return nacelleRadius;
	}

	public int getRotorCount() {
		return 3;
	}

	/**
	 * @return Gesamthöhe incl. Rotoren.
	 */
	public float getTotalHeight() {
		return towerHeight + nacelleRadius * 2 + rotorLength;
	}

	/**
	 * @return Gesamtbreite incl. Rotoren.
	 */
	public float getTotalWidth() {
		return 2 * (nacelleRadius + rotorLength);
	}

	public Point2D.Float getPosition() {
		return position;
	}

	public void moveTo(float x, float y) {
		position.setLocation(x, y);
		erect(towerHeight);
	}

	public void changeRotorPosition(float degrees) {
		setRotorPosition(rotorPosition + degrees);
	}

	public void setRotorPosition(float degrees) {
		if (degrees >= 360) {
			degrees = degrees % 360;
		} else if (degrees < 0) {
			degrees = degrees % 360 + 360;
		}
		rotorPosition = degrees;
	}

	public void setRotorLength(float length) {
		if (length < getMinRotorLength()) {
			throw new IllegalStateException("Rotorlänge zu klein: " + length);
		}
		if (towerHeight - length < getMinBottomDistance()) {
			throw new IllegalStateException("Rotorlänge zu groß: " + length);
		}
		rotorLength = length;
		System.out.println("Neue Rotorlänge: " + length);
	}

	public void setNacelleRadius(float radius) {
		if (radius < getMinNacelleRadius()) {
			throw new IllegalStateException("Nabenradius zu klein: " + radius);
		}
		if (radius > getMaxNacelleRadius()) {
			throw new IllegalStateException("Nabenradius zu groß: " + radius);
		}
		nacelleRadius = radius;
		nacelle.width = nacelle.height = 2 * nacelleRadius;
		nacelle.x = position.x - nacelleRadius;
		nacelle.y = position.y + towerHeight;
		System.out.println("Neuer Nabenradius: " + nacelleRadius);
	}

	public float getMinNacelleRadius() {
		return towerWidthTop / 2;
	}

	public float getMaxNacelleRadius() {
		return towerWidthTop;
	}

	public static float getMinRotorLength() {
		return 20;
	}

	public static float getMinTowerHeight() {
		return 30;
	}

	public static float getMinBottomDistance() {
		return 10;
	}
}