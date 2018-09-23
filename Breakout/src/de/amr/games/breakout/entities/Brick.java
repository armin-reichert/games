package de.amr.games.breakout.entities;

import de.amr.easy.game.entity.SpriteBasedGameEntity;
import de.amr.easy.game.ui.sprites.Sprite;

public class Brick extends SpriteBasedGameEntity {

	public enum BrickColor {
		pink, blue, green, violet, yellow;
	}

	private boolean damaged;
	private int value;

	public Brick(int width, int height, BrickColor color, int value) {
		this.value = value;
		damaged = false;
		tf.setWidth(width);
		tf.setHeight(height);
		sprites.set("s_intact",
				Sprite.ofAssets("Bricks/brick_" + color + "_small.png").scale(width, height));
		sprites.set("s_damaged",
				Sprite.ofAssets("Bricks/brick_" + color + "_small_cracked.png").scale(width, height));
		sprites.select("s_intact");
	}

	public void damage() {
		damaged = true;
		sprites.select("s_damaged");
	}

	public boolean isDamaged() {
		return damaged;
	}

	public int getValue() {
		return value;
	}
}