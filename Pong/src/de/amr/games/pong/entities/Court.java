package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.view.View;

public class Court extends Entity implements View {

	private Color bgColor = Color.BLACK;
	private Color lineColor = Color.WHITE;
	private int lineWidth = 5;

	public Court(Dimension size) {
		tf.setWidth(size.width);
		tf.setHeight(size.height);
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(bgColor);
		g.fillRect(0, 0, tf.getWidth(), tf.getHeight());
		g.setColor(lineColor);
		g.fillRect(0, 0, tf.getWidth(), lineWidth);
		g.fillRect(0, tf.getHeight() - lineWidth, tf.getWidth(), lineWidth);
		g.fillRect(0, 0, lineWidth, tf.getHeight());
		g.fillRect(tf.getWidth() - lineWidth, 0, lineWidth, tf.getHeight());
		g.fillRect(tf.getWidth() / 2 - lineWidth / 2, 0, lineWidth, tf.getHeight());
	}
}