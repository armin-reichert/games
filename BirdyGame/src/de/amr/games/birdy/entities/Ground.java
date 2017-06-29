package de.amr.games.birdy.entities;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.birdy.BirdyGame;

public class Ground extends GameEntity {

	private int width;
	private float startX;

	public Ground(BirdyGame app) {
		setSprites(new Sprite(app.assets, "land"));
		width = currentSprite().getWidth();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		startX -= tr.getVelocityX();
		if (startX < 0) {
			startX = currentSprite().getImage().getWidth(null);
		}
	}

	@Override
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void stopMoving() {
		tr.setVelocityX(0);
	}

	@Override
	public void draw(Graphics2D g) {
		Image image = currentSprite().getImage();
		for (float x = -startX; x < width; x += image.getWidth(null)) {
			g.drawImage(image, (int) x, (int) tr.getY(), null);
		}
	}
}
