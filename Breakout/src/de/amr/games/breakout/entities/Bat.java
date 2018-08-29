package de.amr.games.breakout.entities;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.event.KeyEvent;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;

public class Bat extends GameEntityUsingSprites {

	private final int boardWidth;
	public int speed;

	public Bat(int width, int height, int boardWidth) {
		this.boardWidth = boardWidth;
		addSprite("s_bat", new Sprite("bat_blue.png").scale(width, height));
		setCurrentSprite("s_bat");
		tf.setWidth(currentSprite().getWidth());
		tf.setHeight(currentSprite().getHeight());
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