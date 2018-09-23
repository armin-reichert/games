package de.amr.games.birdy.entities;

import de.amr.easy.game.entity.AbstractGameEntity;

/**
 * A transparent area used for collision handling.
 * 
 * @author Armin Reichert
 */
public class Area extends AbstractGameEntity {

	public Area(int width, int height) {
		tf.setWidth(width);
		tf.setHeight(height);
	}
}
