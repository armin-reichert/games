package de.amr.games.breakout.entities;

import java.awt.Dimension;

import de.amr.easy.game.entity.SpriteBasedGameEntity;
import de.amr.easy.game.ui.sprites.Sprite;

public class Ball extends SpriteBasedGameEntity {

	private Dimension boardSize;

	public Ball(int size) {
		tf.setWidth(size);
		tf.setHeight(size);
		sprites.set("s_ball", Sprite.ofAssets("ball_green.png").scale(size));
		sprites.select("s_ball");
	}

	public void setBoardSize(Dimension boardSize) {
		this.boardSize = boardSize;
	}

	public boolean isOut() {
		return tf.getY() > boardSize.height;
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
}