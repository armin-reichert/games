package de.amr.games.breakout.entities;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.sprites.Sprite;

public class Bat extends Entity {

	private Dimension boardSize;
	public int speed;

	public Bat(int width, int height) {
		Sprite sprite = Sprite.ofAssets("bat_blue.png").scale(width, height);
		sprites.set("s_bat", sprite);
		sprites.select("s_bat");
		tf.setWidth(sprite.getWidth());
		tf.setHeight(sprite.getHeight());
	}

	public void setBoardSize(Dimension boardSize) {
		this.boardSize = boardSize;
	}

	@Override
	public void update() {
		tf.setVelocityX(0);
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			tf.setVelocityX(-speed);
			tf.move();
			tf.setX(min(boardSize.width - tf.getWidth(), max(0, tf.getX())));
		}
		else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			tf.setVelocityX(speed);
			tf.move();
			tf.setX(min(boardSize.width - tf.getWidth(), max(0, tf.getX())));
		}
	}
}