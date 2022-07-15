package de.amr.games.birdy.entities;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameObject;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.easy.game.ui.sprites.SpriteMap;

/**
 * The ground.
 * 
 * @author Armin Reichert
 */
public class Ground extends GameObject {

	private final SpriteMap sprites = new SpriteMap();
	private float startX;

	public Ground() {
		Sprite land = Sprite.ofAssets("land");
		sprites.set("s_land", land);
		sprites.select("s_land");
		tf.width = land.getWidth();
		tf.height = land.getHeight();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		sprites.current().ifPresent(sprite -> {
			sprite.currentAnimationFrame().ifPresent(image -> {
				startX -= tf.vx;
				if (startX < 0) {
					startX = image.getWidth(null);
				}
			});
		});
	}

	public void setWidth(int width) {
		tf.width = width;
		sprites.current().ifPresent(sprite -> sprite.scale(width, sprite.getHeight()));
	}

	@Override
	public void draw(Graphics2D g) {
		sprites.current().ifPresent(sprite -> {
			sprite.currentAnimationFrame().ifPresent(image -> {
				for (float x = -startX; x < tf.width; x += image.getWidth(null)) {
					g.drawImage(image, (int) x, (int) tf.y, null);
				}
			});
		});
	}
}