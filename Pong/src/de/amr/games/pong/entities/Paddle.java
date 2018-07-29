package de.amr.games.pong.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.pong.PongGame;

public class Paddle extends GameEntity {

	protected int width = 15;
	protected int height = 60;
	protected int speed = 5;
	protected Color color = Color.LIGHT_GRAY;
	protected PongGame game;
	private final int paddleUpKey, paddleDownKey;

	public Paddle(PongGame game, int keyUp, int keyDown) {
		this.game = game;
		this.paddleUpKey = keyUp;
		this.paddleDownKey = keyDown;
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
		} else if (tf.getY() >= game.getHeight() - getHeight()) {
			tf.setY(game.getHeight() - getHeight());
		}
		tf.setVelocityY(0);
	}

	@Override
	public Sprite currentSprite() {
		return null;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.empty();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fillRect((int) tf.getX(), (int) tf.getY(), getWidth(), getHeight());
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public boolean hitsBall(Ball ball) {
		return getCollisionBox().intersects(ball.getCollisionBox());
	}
}
