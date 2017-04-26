package de.amr.games.breakout.entities;

import static de.amr.games.breakout.BreakoutGame.Game;
import static de.amr.games.breakout.Globals.BRICK_HEIGHT;
import static de.amr.games.breakout.Globals.BRICK_WIDTH;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Brick extends GameEntity {

	public enum BrickColor {
		blue, green, pink, violet, yellow;
	}

	private boolean cracked;
	private int value;

	public Brick(BrickColor color, int value) {
		this.value = value;
		setSprites(new Sprite(Game.assets, "Bricks/brick_" + color + "_small.png").scale(BRICK_WIDTH, BRICK_HEIGHT),
				new Sprite(Game.assets, "Bricks/brick_" + color + "_small_cracked.png").scale(BRICK_WIDTH, BRICK_HEIGHT));
		cracked = false;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

	@Override
	public Sprite currentSprite() {
		return getSprite(cracked ? 1 : 0);
	}

	public boolean isCracked() {
		return cracked;
	}

	public int getValue() {
		return value;
	}

	public void crack() {
		cracked = true;
	}
}
