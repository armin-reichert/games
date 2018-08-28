package de.amr.games.breakout.entities;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.event.KeyEvent;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.Sprite;

public class Bat extends GameEntityUsingSprites {

	private final int boardWidth;
	public int speed;
	private Sprite s_bat;

	public Bat(int width, int height, int boardWidth) {
		this.boardWidth = boardWidth;
		s_bat = new Sprite("bat_blue.png").scale(width, height);
	}

	@Override
	public int getWidth() {
		return currentSprite().getWidth();
	}

	@Override
	public int getHeight() {
		return currentSprite().getHeight();
	}

	@Override
	public Sprite currentSprite() {
		return s_bat;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_bat);
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

	@Override
	public void init() {
	}
}