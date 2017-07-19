package de.amr.games.breakout.entities;

import java.awt.event.KeyEvent;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;

public class Bat extends GameEntity {

	private final int gameWidth;
	private int speed;

	public Bat(Assets assets, int gameWidth) {
		this.gameWidth = gameWidth;
		setSprites(new Sprite(assets, "Bats/bat_blue.png").scale(50, 10));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			tf.setVelocityX(-speed);
		}
		if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			tf.setVelocityX(speed);
		}
		tf.move();
		tf.setVelocityX(0);
		tf.setX(Math.min(gameWidth - getWidth(), Math.max(0, tf.getX())));
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
}
