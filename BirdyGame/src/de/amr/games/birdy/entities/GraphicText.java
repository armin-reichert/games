package de.amr.games.birdy.entities;

import java.awt.Image;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.sprite.Sprite;

public class GraphicText extends GameEntityUsingSprites {

	private Sprite s_text;

	public GraphicText(Image image) {
		s_text = new Sprite(image);
	}

	@Override
	public int getWidth() {
		return currentSprite().getWidth();
	}

	@Override
	public int getHeight() {
		return currentSprite().getHeight();
	}

	@Override
	public Sprite currentSprite() {
		return s_text;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_text);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
