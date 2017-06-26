package de.amr.games.birdy.entities;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class GameOverText extends GameEntity {

	public GameOverText(Assets assets) {
		super(new Sprite(assets, "text_game_over"));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
