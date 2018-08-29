package de.amr.games.birdy.entities;

import de.amr.easy.game.entity.GameEntity;

/**
 * A transparent area used for collision handling.
 * 
 * @author Armin Reichert
 */
public class Area extends GameEntity {

	public Area(int width, int height) {
		tf.setWidth(width);
		tf.setHeight(height);
	}
}
