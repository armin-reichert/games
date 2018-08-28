package de.amr.samples.marbletoy.entities;

import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

public class Marble extends GameEntityUsingSprites {

	private Sprite sprite;

	public Marble(Sprite sprite) {
		this.sprite = sprite;
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
		return sprite;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(sprite);
	}

	@Override
	public void update() {
		tf.move();
	}

	@Override
	public void init() {
	}
}
