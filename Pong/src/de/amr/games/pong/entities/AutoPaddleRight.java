package de.amr.games.pong.entities;

import java.awt.event.KeyEvent;

public class AutoPaddleRight extends Paddle {

	private float ballRightY;

	public AutoPaddleRight() {
		super(KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
	}

	@Override
	public void update() {
		tf.setVelocityY(0);
		float targetY = courtSize.height / 2 + tf.getHeight();
		if (ball.tf.getVelocityX() > 0) {
			computeBallPositionRight();
			targetY = ballRightY;
		}
		float diff = tf.getY() + tf.getHeight() / 2 - targetY;
		if (diff < -ball.tf.getWidth()) {
			tf.setVelocityY(speed);
		} else if (diff > ball.tf.getWidth()) {
			tf.setVelocityY(-speed);
		}
		move();
	}

	private void computeBallPositionRight() {
		ballRightY = ball.tf.getY() + ball.tf.getHeight() / 2;
		for (float x = ball.tf.getX(); x < courtSize.width - tf.getWidth()
				- ball.tf.getWidth(); x += ball.tf.getVelocityX()) {
			if (ballRightY < 0) {
				ballRightY += ball.tf.getVelocityY();
			} else if (ballRightY >= courtSize.height) {
				ballRightY -= ball.tf.getVelocityY();
			}
		}
	}
}