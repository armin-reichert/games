package de.amr.easy.game.common;

import java.awt.Image;
import java.awt.image.BufferedImage;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;

public class PumpingText extends GameEntity {

	private int frameCount = 6;

	public PumpingText(Application app, String imageName, float delta) {
		Image image = app.assets.image(imageName);
		int height = image.getHeight(null);
		Image[] frames = new Image[frameCount];
		for (int i = 0; i < frameCount; ++i) {
			frames[i] = image.getScaledInstance(-1, Math.round(height + i * delta * height), BufferedImage.SCALE_FAST);
		}
		Sprite sprite = new Sprite(frames);
		setSprites(sprite);
		sprite.createAnimation(AnimationMode.BACK_AND_FORTH, 100);
		sprite.setAnimated(true);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

	public void setPumping(boolean enabled) {
		currentSprite().setAnimated(enabled);
	}
}
