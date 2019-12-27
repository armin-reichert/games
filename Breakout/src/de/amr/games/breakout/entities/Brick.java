package de.amr.games.breakout.entities;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.Entity;
import de.amr.easy.game.math.Vector2f;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;

public class Brick extends Entity {

	public enum BrickColor {
		pink, blue, green, violet, yellow;
	}

	public final SpriteMap sprites = new SpriteMap();
	private boolean damaged;
	private int value;

	public Brick(int width, int height, BrickColor color, int value) {
		this.value = value;
		damaged = false;
		tf.setWidth(width);
		tf.setHeight(height);
		sprites.set("s_intact", Sprite.ofAssets("Bricks/brick_" + color + "_small.png").scale(width, height));
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
	
	@Override
	public void draw(Graphics2D g) {
		sprites.current().ifPresent(sprite -> {
			Vector2f position = tf.getPosition();
			g.translate(position.roundedX(), position.roundedY());
			sprite.draw(g);
			g.translate(-position.roundedX(), -position.roundedY());
		});
	}
}