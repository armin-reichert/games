package de.amr.games.birdy.entities;

import static de.amr.games.birdy.BirdyGame.Game;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class PipeUp extends GameEntity {

	public PipeUp() {
		super(new Sprite(Game.assets, "pipe_up"));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tr.move();
	}
}
