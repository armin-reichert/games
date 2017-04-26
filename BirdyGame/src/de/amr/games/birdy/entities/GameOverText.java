package de.amr.games.birdy.entities;

import static de.amr.games.birdy.BirdyGame.Game;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class GameOverText extends GameEntity {

	public GameOverText() {
		super(new Sprite(Game.assets, "text_game_over"));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
