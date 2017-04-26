package de.amr.games.breakout.entities;

import static de.amr.games.breakout.BreakoutGame.Game;

import java.awt.event.KeyEvent;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Key;
import de.amr.easy.game.sprite.Sprite;

public class Bat extends GameEntity {

	private final int gameWidth;
	private int speed;

	public Bat(int gameWidth) {
		this.gameWidth = gameWidth;
		setSprites(new Sprite(Game.assets, "Bats/bat_blue.png").scale(120, 26));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		if (Key.down(KeyEvent.VK_LEFT)) {
			tr.setVelX(-speed);
		}
		if (Key.down(KeyEvent.VK_RIGHT)) {
			tr.setVelX(speed);
		}
		tr.move();
		tr.setVelX(0);
		tr.setX(Math.min(gameWidth - getWidth(), Math.max(0, tr.getX())));
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
}
