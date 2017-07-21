package de.amr.games.breakout.entities;

import java.awt.Dimension;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.breakout.BreakoutGame;

public class Ball extends GameEntity {

	private final Dimension boardSize;

	public Ball(BreakoutGame app, int size) {
		boardSize = new Dimension(app.getWidth(), app.getHeight());
		Sprite ballSprite = new Sprite(Assets.OBJECT, "ball_green.png").scale(0, size, size);
		setSprites(ballSprite);
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
		if (tf.getX() > boardSize.width - getWidth()) {
			tf.setX(boardSize.width - getWidth());
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
		return tf.getY() > boardSize.height;
	}
}