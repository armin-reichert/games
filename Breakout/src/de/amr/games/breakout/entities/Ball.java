package de.amr.games.breakout.entities;

import static de.amr.games.breakout.Globals.BALL_DIAMETER;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Ball extends GameEntity {

	private final int gameWidth;
	private final int gameHeight;

	public Ball(Assets assets, int gameWidth, int gameHeight) {
		this.gameWidth = gameWidth;
		this.gameHeight = gameHeight;
		setSprites(new Sprite(assets, "Balls/ball_green.png").scale(0, BALL_DIAMETER, BALL_DIAMETER));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.move();
		if (tf.getX() < 0) {
			tf.setX(0);
			if (tf.getVelocityX() < 0) {
				tf.setVelocityX(-tf.getVelocityX());
			}
		}
		if (tf.getX() > gameWidth - getWidth()) {
			tf.setX(gameWidth - getWidth());
			if (tf.getVelocityX() > 0) {
				tf.setVelocityX(-tf.getVelocityX());
			}
		}
		if (tf.getY() < 0) {
			tf.setY(0);
			if (tf.getVelocityY() < 0) {
				tf.setVelocityY(-tf.getVelocityY());
			}
		}
	}

	public boolean isOut() {
		return tf.getY() > gameHeight;
	}
}
