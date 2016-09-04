package de.amr.samples.marbletoy.entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import de.amr.easy.game.entity.GameEntity;

public class Lever extends GameEntity {

	private int size = 30;
	private int legLen = size * 75 / 100;
	private boolean pointsLeft;

	public Lever(int x, int y) {
		tr.moveTo(x, y);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
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
		int x = (int) tr.getX(), y = (int) tr.getY();
		if (pointsLeft) {
			g.drawLine(x + legLen, y - legLen, x - legLen, y + legLen);
		} else {
			g.drawLine(x - legLen, y - legLen, x + legLen, y + legLen);
		}
		g.fillOval(x - size / 2, y - size / 2, size, size);
	}

	@Override
	public Rectangle getCollisionBox() {
		return new Rectangle((int) tr.getX() - size / 2, (int) tr.getY() - size / 2, size / 2,
				size / 2);
	}
}