package de.amr.games.pong.entities;

import java.awt.event.KeyEvent;

import de.amr.games.pong.PongGame;

public class AutoPaddleRight extends Paddle {

	private float ballRightY;

	public AutoPaddleRight(PongGame game) {
		super(game, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
	}

	@Override
	public void update() {
		Ball ball = game.entities.ofClass(Ball.class).findFirst().get();
		float targetY = game.settings.height / 2 + getHeight();
		if (ball.tf.getVelocityX() > 0) {
			computeBallPositionRight();
			targetY = ballRightY;
		}
		float diff = tf.getY() + getHeight() / 2 - targetY;
		if (diff < -ball.getSize()) {
			tf.setVelocityY(speed);
		} else if (diff > ball.getSize()) {
			tf.setVelocityY(-speed);
		}
		moveAndStopAtBorder();
	}

	private void computeBallPositionRight() {
		Ball ball = game.entities.ofClass(Ball.class).findFirst().get();
		ballRightY = ball.tf.getY() + ball.getHeight() / 2;
		for (float x = ball.tf.getX(); x < game.settings.width - getWidth()
				- ball.getWidth(); x += ball.tf.getVelocityX()) {
			if (ballRightY < 0) {
				ballRightY += ball.tf.getVelocityY();
			} else if (ballRightY >= game.settings.height) {
				ballRightY -= ball.tf.getVelocityY();
			}
		}
	}
}
