package de.amr.games.birdy.entities;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.birdy.BirdyGameApp;

/**
 * The ground.
 * 
 * @author Armin Reichert
 */
public class Ground extends GameEntityUsingSprites {

	private float startX;

	public Ground(BirdyGameApp app) {
		addSprite("s_land", new Sprite(Assets.image("land")));
		setCurrentSprite("s_land");
		tf.setWidth(currentSprite().getWidth());
		tf.setHeight(currentSprite().getHeight());
	}

	@Override
	public void update() {
		startX -= tf.getVelocityX();
		if (startX < 0) {
			startX = currentSprite().currentFrame().getWidth(null);
		}
	}

	public void setWidth(int width) {
		tf.setWidth(width);
		currentSprite().scale(width, currentSprite().getHeight());
	}

	public void stopMoving() {
		tf.setVelocityX(0);
	}

	@Override
	public void draw(Graphics2D g) {
		Image image = currentSprite().currentFrame();
		for (float x = -startX; x < tf.getWidth(); x += image.getWidth(null)) {
			g.drawImage(image, (int) x, (int) tf.getY(), null);
		}
	}
}