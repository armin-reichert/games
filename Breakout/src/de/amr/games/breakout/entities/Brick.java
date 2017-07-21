package de.amr.games.breakout.entities;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.breakout.BreakoutGame;

public class Brick extends GameEntity {

	public enum Type {
		pink, blue, green, violet, yellow;
	}

	private boolean damaged;
	private int value;

	public Brick(BreakoutGame app, int width, int height, Type type, int value) {
		this.value = value;
		setSprites(new Sprite(Assets.OBJECT, "Bricks/brick_" + type + "_small.png").scale(width, height),
				new Sprite(Assets.OBJECT, "Bricks/brick_" + type + "_small_cracked.png").scale(width, height));
		damaged = false;
	}

	@Override
	public Sprite currentSprite() {
		return getSprite(damaged ? 1 : 0);
	}

	public void damage() {
		damaged = true;
	}

	public boolean isDamaged() {
		return damaged;
	}

	public int getValue() {
		return value;
	}
}
