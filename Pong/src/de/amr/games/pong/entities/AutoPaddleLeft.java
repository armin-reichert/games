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
		if (ball.tr.getVelX() < 0) {
			computeBallPositionLeft();
			targetY = (int) ballLeftY;
		}
		float diff = tr.getY() + getHeight() / 2 - targetY;
		if (diff < -BALL_SIZE) {
			tr.setVelY(PADDLE_SPEED);
		} else if (diff > BALL_SIZE) {
			tr.setVelY(-PADDLE_SPEED);
		}
		moveAndStopAtBorder();
	}

	private void computeBallPositionLeft() {
		Ball ball = game.entities.findAny(Ball.class);
		ballLeftY = ball.tr.getY() + ball.getHeight() / 2;
		for (float x = ball.tr.getX(); x > getWidth() - 1; x += ball.tr.getVelX()) {
			ballLeftY += ball.tr.getVelY();
			if (ballLeftY < 0) {
				ballLeftY += ball.tr.getVelY();
			} else if (ballLeftY >= game.getHeight()) {
				ballLeftY -= ball.tr.getVelY();
			}
		}
	}
}
