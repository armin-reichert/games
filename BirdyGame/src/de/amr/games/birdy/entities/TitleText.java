package de.amr.games.birdy.entities;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class TitleText extends GameEntity {

	public TitleText() {
		super(new Sprite(Assets.image("title")));
	}
}