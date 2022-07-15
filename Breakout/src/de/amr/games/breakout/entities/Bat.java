package de.amr.games.breakout.entities;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.view.View;

public class Bat extends Entity implements Lifecycle, View {

	private Sprite sprite;
	private Dimension boardSize;
	public int speed;

	public Bat(int width, int height) {
		sprite = Sprite.ofAssets("bat_blue.png").scale(width, height);
		tf.width = (sprite.getWidth());
		tf.height = (sprite.getHeight());
	}

	public void setBoardSize(Dimension boardSize) {
		this.boardSize = boardSize;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.vx = 0;
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			tf.vx = -speed;
			tf.move();
			tf.x = (min(boardSize.width - tf.width, max(0, tf.x)));
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			tf.vx = speed;
			tf.move();
			tf.x = (min(boardSize.width - tf.width, max(0, tf.x)));
		}
	}

	@Override
	public void draw(Graphics2D g) {
		sprite.currentAnimationFrame().ifPresent(frame -> {
			var position = tf.getPosition();
			g.drawImage(frame, position.roundedX(), position.roundedY(), null);
		});
	}
}