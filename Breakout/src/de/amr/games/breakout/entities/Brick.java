package de.amr.games.breakout.entities;

import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Brick extends GameEntity {

	public enum Type {
		pink, blue, green, violet, yellow;
	}

	private boolean damaged;
	private int value;
	private Sprite s_intact;
	private Sprite s_damaged;

	public Brick(int width, int height, Type type, int value) {
		this.value = value;
		s_intact = new Sprite("Bricks/brick_" + type + "_small.png").scale(width, height);
		s_damaged = new Sprite("Bricks/brick_" + type + "_small_cracked.png").scale(width, height);
		damaged = false;
	}

	@Override
	public Sprite currentSprite() {
		return damaged ? s_damaged : s_intact;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_damaged, s_intact);
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

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
