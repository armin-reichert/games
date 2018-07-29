package de.amr.games.birdy.entities;

import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class TitleText extends GameEntity {

	private Sprite s_text;

	public TitleText() {
		s_text = new Sprite(Assets.image("title"));
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