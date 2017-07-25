package de.amr.games.muehle;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;

public class MuehleStein extends GameEntity {

	private final Steinfarbe farbe;
	private int radius;

	public MuehleStein(Steinfarbe farbe) {
		this.farbe = farbe;
		radius = 20;
	}

	public Steinfarbe getFarbe() {
		return farbe;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX() - radius, tf.getY() - radius);
		g.setColor(farbe == Steinfarbe.WEISS ? Color.ORANGE : Color.BLACK);
		g.fillOval(0, 0, 2 * radius, 2 * radius);
		g.translate(-tf.getX() + radius, -tf.getY() + radius);
	}
}