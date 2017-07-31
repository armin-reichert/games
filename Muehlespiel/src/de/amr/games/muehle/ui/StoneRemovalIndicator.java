package de.amr.games.muehle.ui;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.games.muehle.board.StoneType;

public class StoneRemovalIndicator extends Stone {

	public StoneRemovalIndicator(StoneType stoneColor) {
		super(stoneColor);
	}

	public void setStoneColor(StoneType color) {
		this.color = color;
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		g.translate(tf.getX() - getWidth() / 2, tf.getY() - getHeight() / 2);
		g.setColor(Color.RED);
		g.drawLine(0, 0, getWidth(), getHeight());
		g.drawLine(0, getHeight(), getWidth(), 0);
		g.translate(-tf.getX() + getWidth() / 2, -tf.getY() + getHeight() / 2);
	}
}