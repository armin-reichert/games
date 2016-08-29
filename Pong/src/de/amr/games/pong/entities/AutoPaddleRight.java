package de.amr.games.pong.entities;

import static de.amr.games.pong.Globals.BALL_SIZE;
import static de.amr.games.pong.Globals.PADDLE_SPEED;

import java.awt.event.KeyEvent;

import de.amr.games.pong.PongGame;

public class AutoPaddleRight extends Paddle {

	private float ballRightY;

	public AutoPaddleRight(PongGame game) {
		super(game, KeyEvent.CHAR_UNDEFINED, KeyEvent.CHAR_UNDEFINED);
	}

	@Override
	public void update() {
		Ball ball = PongGame.Entities.findAny(Ball.class);
		float targetY = game.getHeight() / 2 + getHeight();
		if (ball.tr.getVelX() > 0) {
			computeBallPositionRight();
			targetY = ballRightY;
		}
		float diff = tr.getY() + getHeight() / 2 - targetY;
		if (diff < -BALL_SIZE) {
			tr.setVelY(PADDLE_SPEED);
		} else if (diff > BALL_SIZE) {
			tr.setVelY(-PADDLE_SPEED);
		}
		moveAndStopAtBorder();
	}

	private void computeBallPositionRight() {
		Ball ball = PongGame.Entities.findAny(Ball.class);
		ballRightY = ball.tr.getY() + ball.getHeight() / 2;
		for (float x = ball.tr.getX(); x < game.getWidth() - getWidth() - ball.getWidth(); x += ball.tr
				.getVelX()) {
			if (ballRightY < 0) {
				ballRightY += ball.tr.getVelY();
			} else if (ballRightY >= game.getHeight()) {
				ballRightY -= ball.tr.getVelY();
			}
		}
	}
}
