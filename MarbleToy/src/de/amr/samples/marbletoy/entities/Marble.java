package de.amr.samples.marbletoy.entities;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

public class Marble extends GameEntityUsingSprites {

	public Marble(Sprite sprite) {
		addSprite("s_marble", sprite);
		setCurrentSprite("s_marble");
		tf.setWidth(currentSprite().getWidth());
		tf.setHeight(currentSprite().getHeight());
	}

	@Override
	public void update() {
		tf.move();
	}
}