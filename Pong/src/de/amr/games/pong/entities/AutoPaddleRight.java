package de.amr.games.pong.entities;

import static de.amr.games.pong.PongGlobals.BALL_SIZE;
import static de.amr.games.pong.PongGlobals.PADDLE_SPEED;

import java.awt.event.KeyEvent;

import de.amr.games.pong.PongGame;

public class AutoPaddleRight extends Paddle {

	private float ballRightY;

	public AutoPaddleRight(PongGame game) {
		super(game, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
	}

	@Override
	public void update() {
		Ball ball = game.entities.findAny(Ball.class);
		float targetY = game.getHeight() / 2 + getHeight();
		if (ball.tf.getVelocityX() > 0) {
			computeBallPositionRight();
			targetY = ballRightY;
		}
		float diff = tf.getY() + getHeight() / 2 - targetY;
		if (diff < -BALL_SIZE) {
			tf.setVelocityY(PADDLE_SPEED);
		} else if (diff > BALL_SIZE) {
			tf.setVelocityY(-PADDLE_SPEED);
		}
		moveAndStopAtBorder();
	}

	private void computeBallPositionRight() {
		Ball ball = game.entities.findAny(Ball.class);
		ballRightY = ball.tf.getY() + ball.getHeight() / 2;
		for (float x = ball.tf.getX(); x < game.getWidth() - getWidth() - ball.getWidth(); x += ball.tf.getVelocityX()) {
			if (ballRightY < 0) {
				ballRightY += ball.tf.getVelocityY();
			} else if (ballRightY >= game.getHeight()) {
				ballRightY -= ball.tf.getVelocityY();
			}
		}
	}
}
