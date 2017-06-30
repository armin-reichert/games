package de.amr.games.pong.entities;

import static de.amr.games.pong.PongGlobals.BALL_COLOR;
import static de.amr.games.pong.PongGlobals.BALL_SIZE;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.view.Drawable;

public class Ball extends GameEntity implements Drawable {

	private final int gameHeight;

	public Ball(int gameHeight) {
		this.gameHeight = gameHeight;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.move();
		if (tf.getY() < 0) {
			tf.setY(0);
			tf.setVelocityY(-tf.getVelocityY());
		} else if (tf.getY() >= gameHeight - getHeight()) {
			tf.setY(gameHeight - getHeight() - 1);
			tf.setVelocityY(-tf.getVelocityY());
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(BALL_COLOR);
		g.fillOval((int) tf.getX(), (int) tf.getY(), BALL_SIZE, BALL_SIZE);
	}

	@Override
	public int getWidth() {
		return BALL_SIZE;
	}

	@Override
	public int getHeight() {
		return BALL_SIZE;
	}
}
