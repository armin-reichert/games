package de.amr.games.breakout.entities;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.breakout.BreakoutGame;

public class Bat extends GameEntity {

	private final Dimension boardSize;
	public int speed;

	public Bat(BreakoutGame app, int width, int height) {
		boardSize = new Dimension(app.getWidth(), app.getHeight());
		setSprites(new Sprite(Assets.OBJECT, "bat_blue.png").scale(width, height));
	}

	@Override
	public void update() {
		tf.setVelocityX(0);
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			tf.setVelocityX(-speed);
			tf.move();
			tf.setX(min(boardSize.width - getWidth(), max(0, tf.getX())));
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			tf.setVelocityX(speed);
			tf.move();
			tf.setX(min(boardSize.width - getWidth(), max(0, tf.getX())));
		}
	}
}