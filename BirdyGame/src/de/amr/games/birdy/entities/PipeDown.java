package de.amr.games.birdy.entities;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class PipeDown extends GameEntity {

	public PipeDown(Assets assets) {
		super(new Sprite(assets, "pipe_down"));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tr.move();
	}

}
