package de.amr.games.pong.entities;

import static java.awt.event.KeyEvent.VK_UNDEFINED;

public class AutoPaddleLeft extends Paddle {

	private float ballLeftY;

	public AutoPaddleLeft() {
		super(VK_UNDEFINED, VK_UNDEFINED);
	}

	@Override
	public void update() {
		tf.setVelocityY(0);
		int targetY = courtSize.height / 2 + tf.getHeight();
		if (ball.tf.getVelocityX() < 0) {
			computeBallPositionLeft();
			targetY = (int) ballLeftY;
		}
		float diff = tf.getY() + tf.getHeight() / 2 - targetY;
		if (diff < -ball.tf.getWidth()) {
			tf.setVelocityY(speed);
		} else if (diff > ball.tf.getWidth()) {
			tf.setVelocityY(-speed);
		}
		move();
	}

	private void computeBallPositionLeft() {
		ballLeftY = ball.tf.getY() + ball.tf.getHeight() / 2;
		for (float x = ball.tf.getX(); x > tf.getWidth() - 1; x += ball.tf.getVelocityX()) {
			ballLeftY += ball.tf.getVelocityY();
			if (ballLeftY < 0) {
				ballLeftY += ball.tf.getVelocityY();
			} else if (ballLeftY >= courtSize.height) {
				ballLeftY -= ball.tf.getVelocityY();
			}
		}
	}
}