package de.amr.games.birdy.entities;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Ground extends GameEntity {

	private int width;
	private float startX;

	public Ground(Assets assets) {
		setSprites(new Sprite(assets, "land"));
		width = currentSprite().getWidth();
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		startX -= tr.getVelX();
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
		tr.setVelX(0);
	}

	@Override
	public void draw(Graphics2D g) {
		Image image = currentSprite().getImage();
		for (float x = -startX; x < width; x += image.getWidth(null)) {
			g.drawImage(image, (int) x, (int) tr.getY(), null);
		}
	}
}
