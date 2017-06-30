package de.amr.games.pong.entities;

import static de.amr.games.pong.PongGlobals.PADDLE_COLOR;
import static de.amr.games.pong.PongGlobals.PADDLE_HEIGHT;
import static de.amr.games.pong.PongGlobals.PADDLE_SPEED;
import static de.amr.games.pong.PongGlobals.PADDLE_WIDTH;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.pong.PongGame;

public class Paddle extends GameEntity {

	protected PongGame game;
	private final int paddleUpKey, paddleDownKey;

	public Paddle(PongGame game, int keyUp, int keyDown) {
		this.game = game;
		this.paddleUpKey = keyUp;
		this.paddleDownKey = keyDown;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (Keyboard.keyDown(paddleUpKey)) {
			tf.setVelocityY(-PADDLE_SPEED);
		}
		if (Keyboard.keyDown(paddleDownKey)) {
			tf.setVelocityY(PADDLE_SPEED);
		}
		moveAndStopAtBorder();
	}

	protected void moveAndStopAtBorder() {
		tf.move();
		if (tf.getY() < 0) {
			tf.setY(0);
		} else if (tf.getY() >= game.getHeight() - getHeight()) {
			tf.setY(game.getHeight() - getHeight());
		}
		tf.setVelocityY(0);
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(PADDLE_COLOR);
		g.fillRect((int) tf.getX(), (int) tf.getY(), getWidth(), getHeight());
	}

	@Override
	public int getWidth() {
		return PADDLE_WIDTH;
	}

	@Override
	public int getHeight() {
		return PADDLE_HEIGHT;
	}

	public boolean hitsBall(Ball ball) {
		return getCollisionBox().intersects(ball.getCollisionBox());
	}
}
