package de.amr.games.birdy.entities;

import static de.amr.games.birdy.BirdyGame.Game;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class TitleText extends GameEntity {

	public TitleText() {
		super(new Sprite(Game.assets, "title"));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}
}
