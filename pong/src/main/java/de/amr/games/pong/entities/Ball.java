package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameObject;

public class Ball extends GameObject {

	public int maxY;
	public Color color;

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.move();
		if (tf.y <= 0) {
			tf.y = 0;
			tf.vy *= -1;
		} else if (tf.y + tf.height >= maxY) {
			tf.y = (maxY - 1 - tf.height);
			tf.vy *= -1;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fillOval((int) tf.x, (int) tf.y, tf.width, tf.height);
	}
}