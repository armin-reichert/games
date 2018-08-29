package de.amr.games.breakout.entities;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

public class Brick extends GameEntityUsingSprites {

	public enum Type {
		pink, blue, green, violet, yellow;
	}

	private boolean damaged;
	private int value;

	public Brick(int width, int height, Type type, int value) {
		this.value = value;
		addSprite("s_intact", new Sprite("Bricks/brick_" + type + "_small.png").scale(width, height));
		addSprite("s_damaged",
				new Sprite("Bricks/brick_" + type + "_small_cracked.png").scale(width, height));
		damaged = false;
		setCurrentSprite("s_intact");
	}

	public void damage() {
		damaged = true;
		setCurrentSprite("s_damaged");
	}

	public boolean isDamaged() {
		return damaged;
	}

	public int getValue() {
		return value;
	}
}
