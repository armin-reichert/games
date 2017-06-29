package de.amr.games.birdy.entities;

import de.amr.easy.game.entity.GameEntity;

/**
 * A transparent area used for collision handling.
 * 
 * @author Armin Reichert
 */
public class Area extends GameEntity {

	private int width;
	private int height;

	public Area(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
