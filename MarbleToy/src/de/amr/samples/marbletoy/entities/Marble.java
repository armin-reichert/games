package de.amr.samples.marbletoy.entities;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;

public class Marble extends GameEntity {

	public Marble(Sprite sprite) {
		super(sprite);
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
		tf.move();
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		// g.setColor(Color.BLACK);
		// g.fill(getCollisionBox());
	}

}
