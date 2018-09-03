package de.amr.games.breakout.entities;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

public class Brick extends GameEntityUsingSprites {

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
		setSprite("s_intact",
				Sprite.ofAssets("Bricks/brick_" + color + "_small.png").scale(width, height));
		setSprite("s_damaged",
				Sprite.ofAssets("Bricks/brick_" + color + "_small_cracked.png").scale(width, height));
		setSelectedSprite("s_intact");
	}

	public void damage() {
		damaged = true;
		setSelectedSprite("s_damaged");
	}

	public boolean isDamaged() {
		return damaged;
	}

	public int getValue() {
		return value;
	}
}