package de.amr.easy.game.sprite;

import static de.amr.easy.game.Application.Assets;

import java.awt.Graphics2D;
import java.awt.Image;

import de.amr.easy.game.view.Drawable;

public class Sprite implements Drawable {

	protected Image[] images;
	protected Animation animation;

	public Sprite(Image... images) {
		if (images.length == 0) {
			throw new IllegalArgumentException("Sprite needs at least one image");
		}
		this.images = images;
	}

	public Sprite(String... imageKeys) {
		if (imageKeys.length == 0) {
			throw new IllegalArgumentException("Sprite needs at least one image");
		}
		images = new Image[imageKeys.length];
		int i = 0;
		for (String key : imageKeys) {
			images[i++] = Assets.image(key);
		}
	}

	public Sprite scale(int index, int width, int height) {
		if (index < 0 || index >= images.length) {
			throw new IllegalArgumentException("Sprite index out of range: " + index);
		}
		images[index] = Assets.scaledImage(images[index], width, height);
		return this;
	}

	public Sprite scale(int width, int height) {
		for (int i = 0; i < images.length; ++i) {
			images[i] = Assets.scaledImage(images[i], width, height);
		}
		return this;
	}

	public Image getImage() {
		return animation == null ? images[0] : animation.getCurrentFrame();
	}

	public int getWidth() {
		return getImage().getWidth(null);
	}

	public int getHeight() {
		return getImage().getHeight(null);
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(getImage(), 0, 0, null);
		if (animation != null && animation.isEnabled()) {
			animation.update();
		}
	}

	public void createAnimation(AnimationMode mode, int frameDurationMillis) {
		if (mode == AnimationMode.LEFT_TO_RIGHT) {
			animation = new LeftToRightAnimation(images);
		} else if (mode == AnimationMode.BACK_AND_FORTH) {
			animation = new BackForthAnimation(images);
		} else if (mode == AnimationMode.CYCLIC) {
			animation = new CyclicAnimation(images);
		} else {
			throw new IllegalArgumentException();
		}
		animation.setFrameDuration(frameDurationMillis);
		animation.setEnabled(true);
	}
	
	public void setAnimated(boolean enabled) {
		if (animation == null && images != null && images.length > 1) {
			createAnimation(AnimationMode.BACK_AND_FORTH, 300);
		}
		if (animation != null) {
			animation.setEnabled(enabled);
		}
	}
	
	public void resetAnimation() {
		if (animation != null) {
			animation.reset();
		}
	}
}
