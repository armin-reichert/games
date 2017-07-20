package de.amr.games.breakout.entities;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.breakout.BreakoutGame;

public class Brick extends GameEntity {

	public enum Type {
		blue, green, pink, violet, yellow;
	}

	private boolean cracked;
	private int value;

	public Brick(BreakoutGame app, int width, int height, Type type, int value) {
		this.value = value;
		setSprites(new Sprite(app.assets, "Bricks/brick_" + type + "_small.png").scale(width, height),
				new Sprite(app.assets, "Bricks/brick_" + type + "_small_cracked.png").scale(width, height));
		cracked = false;
	}

	@Override
	public Sprite currentSprite() {
		return getSprite(cracked ? 1 : 0);
	}

	public void crack() {
		cracked = true;
	}

	public boolean isCracked() {
		return cracked;
	}

	public int getValue() {
		return value;
	}
}
