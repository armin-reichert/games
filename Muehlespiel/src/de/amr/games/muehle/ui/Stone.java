package de.amr.games.muehle.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.muehle.board.StoneType;

public class Stone extends GameEntity {

	public static int radius = 20;

	protected StoneType color;

	public Stone(StoneType color) {
		this.color = color;
	}

	public StoneType getColor() {
		return color;
	}

	@Override
	public int getWidth() {
		return 2 * radius;
	}

	@Override
	public int getHeight() {
		return 2 * radius;
	}

	@Override
	public void draw(Graphics2D g) {
		g.translate(tf.getX() - radius, tf.getY() - radius);
		g.setColor(color == StoneType.WHITE ? new Color(255, 248, 220) : Color.DARK_GRAY);
		g.fillOval(0, 0, 2 * radius, 2 * radius);
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2));
		g.drawOval(0, 0, 2 * radius, 2 * radius);
		g.translate(-tf.getX() + radius, -tf.getY() + radius);
	}
}
