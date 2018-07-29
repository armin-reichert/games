package de.amr.samples.marbletoy.entities;

import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Marble extends GameEntity {

	private Sprite sprite;

	public Marble(Sprite sprite) {
		this.sprite = sprite;
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
}
