package de.amr.games.breakout.entities;

import java.awt.Dimension;
import java.awt.Graphics2D;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.view.View;

public class Ball extends Entity implements Lifecycle, View {

	private Sprite sprite;
	private Dimension boardSize;

	public Ball(int size) {
		tf.width =(size);
		tf.height =(size);
		sprite = Sprite.ofAssets("ball_green.png").scale(size);
	}

	public void setBoardSize(Dimension boardSize) {
		this.boardSize = boardSize;
	}

	public boolean isOut() {
		return tf.y > boardSize.height;
	}

	@Override
	public void draw(Graphics2D g2) {
		Graphics2D g = (Graphics2D) g2.create();
		g.translate(tf.x, tf.y);
		sprite.draw(g);
		g.dispose();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.move();
		if (tf.x < 0) {
			tf.x=(0);
			if (tf.getVelocityX() < 0) {
				tf.setVelocityX(-tf.getVelocityX());
			}
		}
		if (tf.x > boardSize.width - tf.width) {
			tf.x=(boardSize.width - tf.width);
			if (tf.getVelocityX() > 0) {
				tf.setVelocityX(-tf.getVelocityX());
			}
		}
		if (tf.y < 0) {
			tf.y=(0);
			if (tf.getVelocityY() < 0) {
				tf.setVelocityY(-tf.getVelocityY());
			}
		}
	}
}