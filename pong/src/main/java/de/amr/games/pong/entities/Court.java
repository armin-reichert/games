package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.View;

public class Court extends Entity implements View {

	public Color floorColor;
	public Color lineColor;
	public int lineWidth;

	@Override
	public void draw(Graphics2D g) {
		g.setColor(floorColor);
		g.fillRect(0, 0, tf.width, tf.height);
		g.setColor(lineColor);
		g.fillRect(0, 0, tf.width, lineWidth);
		g.fillRect(0, tf.height - lineWidth, tf.width, lineWidth);
		g.fillRect(0, 0, lineWidth, tf.height);
		g.fillRect(tf.width - lineWidth, 0, lineWidth, tf.height);
		g.fillRect(tf.width / 2 - lineWidth / 2, 0, lineWidth, tf.height);
	}
}