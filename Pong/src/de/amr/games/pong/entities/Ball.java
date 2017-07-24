package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.view.Drawable;

public class Ball extends GameEntity implements Drawable {

	private final int courtHeight;
	private int size = 16;
	private Color color = Color.YELLOW;

	public Ball(int courtHeight) {
		this.courtHeight = courtHeight;
	}

	@Override
	public void update() {
		tf.move();
		if (tf.getY() < 0) {
			tf.setY(0);
			tf.setVelocityY(-tf.getVelocityY());
		} else if (tf.getY() >= courtHeight - getHeight()) {
			tf.setY(courtHeight - getHeight() - 1);
			tf.setVelocityY(-tf.getVelocityY());
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fillOval((int) tf.getX(), (int) tf.getY(), size, size);
	}

	@Override
	public int getWidth() {
		return size;
	}

	@Override
	public int getHeight() {
		return size;
	}

	public int getSize() {
		return size;
	}
}