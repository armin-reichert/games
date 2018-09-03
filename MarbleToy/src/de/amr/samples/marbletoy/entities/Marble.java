package de.amr.samples.marbletoy.entities;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

public class Marble extends GameEntityUsingSprites {

	public Marble() {
		setSprite("s_marble", Sprite.ofAssets("marble.png").scale(50));
		setSelectedSprite("s_marble");
		tf.setWidth(getSelectedSprite().getWidth());
		tf.setHeight(getSelectedSprite().getHeight());
	}

	@Override
	public void update() {
		tf.move();
	}
}