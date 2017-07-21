package de.amr.games.breakout.entities;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.event.KeyEvent;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;

public class Bat extends GameEntity {

	private final int boardWidth;
	public int speed;

	public Bat(int width, int height, int boardWidth) {
		this.boardWidth = boardWidth;
		setSprites(new Sprite("bat_blue.png").scale(width, height));
	}

	@Override
	public void update() {
		tf.setVelocityX(0);
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			tf.setVelocityX(-speed);
			tf.move();
			tf.setX(min(boardWidth - getWidth(), max(0, tf.getX())));
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			tf.setVelocityX(speed);
			tf.move();
			tf.setX(min(boardWidth - getWidth(), max(0, tf.getX())));
		}
	}
}