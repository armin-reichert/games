package de.amr.games.birdy.entities;

import de.amr.easy.game.entity.GameEntity;

public class Area extends GameEntity {

	private final int width;
	private final int height;

	public Area(int x, int y, int width, int height) {
		tr.moveTo(x, y);
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
}
