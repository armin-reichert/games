package de.amr.games.birdy.entities;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.birdy.play.BirdyGame;

/**
 * The ground.
 * 
 * @author Armin Reichert
 */
public class Ground extends GameEntity {

	private float startX;

	public Ground(BirdyGame app) {
		setSprites(new Sprite(app.assets.image("land")));
	}

	@Override
	public void update() {
		startX -= tf.getVelocityX();
		if (startX < 0) {
			startX = currentSprite().getImage().getWidth(null);
		}
	}

	public void setWidth(int width) {
		currentSprite().scale(width, getHeight());
	}

	public void stopMoving() {
		tf.setVelocityX(0);
	}

	@Override
	public void draw(Graphics2D g) {
		Image image = currentSprite().getImage();
		for (float x = -startX; x < getWidth(); x += image.getWidth(null)) {
			g.drawImage(image, (int) x, (int) tf.getY(), null);
		}
	}
}
