package de.amr.samples.marbletoy.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.view.View;

public class Lever extends GameEntity implements View {

	private int size = 30;
	private int legLen = size * 75 / 100;
	private boolean pointsLeft;

	public Lever(int x, int y) {
		tf.moveTo(x, y);
		tf.setWidth(size);
		tf.setHeight(size);
	}

	public boolean pointsLeft() {
		return pointsLeft;
	}

	public void setPointsLeft(boolean left) {
		this.pointsLeft = left;
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(new Color(200, 200, 200));
		g.setStroke(new BasicStroke(4));
		int x = (int) tf.getX(), y = (int) tf.getY();
		if (pointsLeft) {
			g.drawLine(x + legLen, y - legLen, x - legLen, y + legLen);
		} else {
			g.drawLine(x - legLen, y - legLen, x + legLen, y + legLen);
		}
		// g.fillOval(x - size / 2, y - size / 2, size, size);
		// g.setColor(Color.BLACK);
		// g.fill(getCollisionBox());
	}

	@Override
	public Rectangle getCollisionBox() {
		return new Rectangle((int) tf.getX() - size / 2, (int) tf.getY() - size / 2, size / 2, size / 2);
	}
}