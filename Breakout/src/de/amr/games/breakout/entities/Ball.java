package de.amr.games.breakout.entities;

import java.awt.Dimension;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.breakout.BreakoutGame;

public class Ball extends GameEntityUsingSprites {

	private final Dimension boardSize;

	public Ball(BreakoutGame app, int size) {
		boardSize = new Dimension(app.settings.width, app.settings.height);
		addSprite("s_ball", new Sprite("ball_green.png").scaleFrame(0, size, size));
		setCurrentSprite("s_ball");
		tf.setWidth(currentSprite().getWidth());
		tf.setHeight(currentSprite().getHeight());
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
		if (tf.getX() > boardSize.width - tf.getWidth()) {
			tf.setX(boardSize.width - tf.getWidth());
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