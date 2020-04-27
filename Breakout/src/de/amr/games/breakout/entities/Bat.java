package de.amr.games.breakout.entities;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.view.View;

public class Bat extends Entity implements Lifecycle, View {

	private Sprite sprite;
	private Dimension boardSize;
	public int speed;

	public Bat(int width, int height) {
		sprite = Sprite.ofAssets("bat_blue.png").scale(width, height);
		tf.setWidth(sprite.getWidth());
		tf.setHeight(sprite.getHeight());
	}

	public void setBoardSize(Dimension boardSize) {
		this.boardSize = boardSize;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.setVelocityX(0);
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			tf.setVelocityX(-speed);
			tf.move();
			tf.setX(min(boardSize.width - tf.getWidth(), max(0, tf.getX())));
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			tf.setVelocityX(speed);
			tf.move();
			tf.setX(min(boardSize.width - tf.getWidth(), max(0, tf.getX())));
		}
	}

	@Override
	public void draw(Graphics2D g) {
		Vector2f position = tf.getPosition();
		g.drawImage(sprite.currentFrame(), position.roundedX(), position.roundedY(), null);
	}
}