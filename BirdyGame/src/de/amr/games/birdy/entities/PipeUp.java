package de.amr.games.birdy.entities;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class PipeUp extends GameEntity {

	public PipeUp() {
		super(new Sprite("pipe_up"));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tr.move();
	}
}
