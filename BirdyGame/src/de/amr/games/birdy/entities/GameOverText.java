package de.amr.games.birdy.entities;

import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class GameOverText extends GameEntity {

	private Sprite s_text;

	public GameOverText() {
		s_text = new Sprite(Assets.image("text_game_over"));
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