package de.amr.games.muehle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;

public class Stein extends GameEntity {

	private SteinFarbe color;
	private int radius;

	public Stein(SteinFarbe farbe, int radius) {
		this.color = farbe;
		this.radius = radius;
	}

	public SteinFarbe getColor() {
		return color;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX() - radius, tf.getY() - radius);
		g.setColor(color == SteinFarbe.HELL ? new Color(255, 248, 220) : Color.DARK_GRAY);
		g.fillOval(0, 0, 2 * radius, 2 * radius);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2));
		g.drawOval(0, 0, 2 * radius, 2 * radius);
		g.translate(-tf.getX() + radius, -tf.getY() + radius);
	}
}