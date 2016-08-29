package de.amr.samples.marbletoy.entities;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Marble extends GameEntity {

	public Marble() {
		super(new Sprite("marble.png").scale(50, 50));
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tr.move();
	}

}
