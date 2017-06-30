package de.amr.games.pong.entities;

import static de.amr.games.pong.PongGlobals.BALL_SIZE;
import static de.amr.games.pong.PongGlobals.PADDLE_SPEED;
import static java.awt.event.KeyEvent.VK_UNDEFINED;

import de.amr.games.pong.PongGame;

public class AutoPaddleLeft extends Paddle {

	private float ballLeftY;

	public AutoPaddleLeft(PongGame game) {
		super(game, VK_UNDEFINED, VK_UNDEFINED);
	}

	@Override
	public void update() {
		Ball ball = game.entities.findAny(Ball.class);
		int targetY = game.getHeight() / 2 + getHeight();
		if (ball.tf.getVelocityX() < 0) {
			computeBallPositionLeft();
			targetY = (int) ballLeftY;
		}
		float diff = tf.getY() + getHeight() / 2 - targetY;
		if (diff < -BALL_SIZE) {
			tf.setVelocityY(PADDLE_SPEED);
		} else if (diff > BALL_SIZE) {
			tf.setVelocityY(-PADDLE_SPEED);
		}
		moveAndStopAtBorder();
	}

	private void computeBallPositionLeft() {
		Ball ball = game.entities.findAny(Ball.class);
		ballLeftY = ball.tf.getY() + ball.getHeight() / 2;
		for (float x = ball.tf.getX(); x > getWidth() - 1; x += ball.tf.getVelocityX()) {
			ballLeftY += ball.tf.getVelocityY();
			if (ballLeftY < 0) {
				ballLeftY += ball.tf.getVelocityY();
			} else if (ballLeftY >= game.getHeight()) {
				ballLeftY -= ball.tf.getVelocityY();
			}
		}
	}
}
