package de.amr.games.muehle;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;

public class Stein extends GameEntity {

	private final Farbe farbe;
	private int radius;

	public Stein(Farbe farbe) {
		this.farbe = farbe;
		radius = 20;
	}

	public Farbe getFarbe() {
		return farbe;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX() - radius, tf.getY() - radius);
		g.setColor(farbe == Farbe.WEISS ? Color.ORANGE : Color.BLACK);
		g.fillOval(0, 0, 2 * radius, 2 * radius);
		g.translate(-tf.getX() + radius, -tf.getY() + radius);
	}
}