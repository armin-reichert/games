package de.amr.games.birdy.entities;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class GameOverText extends GameEntity {

	public GameOverText() {
		super(new Sprite(Assets.image("text_game_over")));
	}
}