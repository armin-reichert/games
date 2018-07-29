package de.amr.games.birdy.entities;

import java.awt.Image;
import java.util.stream.Stream;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class GraphicText extends GameEntity {

	private Sprite s_text;

	public GraphicText(Image image) {
		s_text = new Sprite(image);
	}

	@Override
	public Sprite currentSprite() {
		return s_text;
	}

	@Override
	public Stream<Sprite> getSprites() {
		return Stream.of(s_text);
	}
}
