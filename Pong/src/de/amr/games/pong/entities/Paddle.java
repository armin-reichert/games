package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.games.pong.PongGameApp;

public class Paddle extends GameEntity implements View {

	protected int speed = 5;
	protected Color color = Color.LIGHT_GRAY;
	protected PongGameApp game;
	private final int paddleUpKey, paddleDownKey;

	public Paddle(PongGameApp game, int keyUp, int keyDown) {
		this.game = game;
		this.paddleUpKey = keyUp;
		this.paddleDownKey = keyDown;
		tf.setWidth(15);
		tf.setHeight(60);
	}

	@Override
	public void update() {
		if (Keyboard.keyDown(paddleUpKey)) {
			tf.setVelocityY(-speed);
		}
		if (Keyboard.keyDown(paddleDownKey)) {
			tf.setVelocityY(speed);
		}
		moveAndStopAtBorder();
	}

	protected void moveAndStopAtBorder() {
		tf.move();
		if (tf.getY() < 0) {
			tf.setY(0);
		} else if (tf.getY() >= game.settings.height - tf.getHeight()) {
			tf.setY(game.settings.height - tf.getHeight());
		}
		tf.setVelocityY(0);
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fillRect((int) tf.getX(), (int) tf.getY(), tf.getWidth(), tf.getHeight());
	}

	public boolean hitsBall(Ball ball) {
		return getCollisionBox().intersects(ball.getCollisionBox());
	}
}