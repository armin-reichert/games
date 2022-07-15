package de.amr.games.pong.entities;

public class AutoPaddleRight extends Paddle {

	private float ballRightY;

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.vy = 0;
		float targetY = maxY / 2 + tf.height;
		if (ball.tf.vx > 0) {
			computeBallPositionRight();
			targetY = ballRightY;
		}
		float diff = tf.y + tf.height / 2 - targetY;
		if (diff < -ball.tf.width) {
			tf.vy = speed;
		} else if (diff > ball.tf.width) {
			tf.vy = -speed;
		}
		move();
	}

	private void computeBallPositionRight() {
		ballRightY = ball.tf.y + ball.tf.height / 2;
		for (float x = ball.tf.x; x < maxX - tf.width - ball.tf.width; x += ball.tf.vx) {
			if (ballRightY < 0) {
				ballRightY += ball.tf.vy;
			} else if (ballRightY >= maxY) {
				ballRightY -= ball.tf.vy;
			}
		}
	}
}