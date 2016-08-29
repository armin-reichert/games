package de.amr.games.birdy.entities;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class PipeDown extends GameEntity {

	public PipeDown() {
		super(new Sprite("pipe_down"));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tr.move();
	}

}
