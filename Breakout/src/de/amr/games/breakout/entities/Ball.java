package de.amr.games.breakout.entities;

import java.awt.Dimension;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.breakout.BreakoutGame;

public class Ball extends GameEntity {

	private final Dimension boardSize;
	private Sprite s_ball;

	public Ball(BreakoutGame app, int size) {
		boardSize = new Dimension(app.settings.width, app.settings.height);
		s_ball = new Sprite("ball_green.png").scaleFrame(0, size, size);
	}

	@Override
	public int getWidth() {
		return currentSprite().getWidth();
	}

	@Override
	public int getHeight() {
		return currentSprite().getHeight();
	}

	@Override
	public Sprite currentSprite() {
		return s_ball;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_ball);
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

	@Override
	public void init() {
	}
}