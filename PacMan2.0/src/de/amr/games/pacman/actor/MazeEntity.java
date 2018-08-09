package de.amr.games.pacman.actor;

import static de.amr.games.pacman.ui.Spritesheet.TS;
import static java.lang.Math.round;

import java.awt.Graphics2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.games.pacman.model.Tile;

public abstract class MazeEntity extends GameEntity {

	public void placeAt(Tile tile) {
		placeAt(tile.col, tile.row);
	}

	public void placeAt(int col, int row) {
		tf.moveTo(col * TS, row * TS);
	}

	public boolean isExactlyOverTile() {
		return round(tf.getX()) % TS == 0 && round(tf.getY()) % TS == 0;
	}

	public Tile getTile() {
		return new Tile(col(), row());
	}

	public int row() {
		return round(tf.getY() + getHeight() / 2) / TS;
	}

	public int col() {
		return round(tf.getX() + getWidth() / 2) / TS;
	}

	@Override
	public void draw(Graphics2D g) {
		// by default, draw sprite centered over collision box
		int dx = (getWidth() - currentSprite().getWidth()) / 2;
		int dy = (getHeight() - currentSprite().getHeight()) / 2;
		g.translate(dx, dy);
		super.draw(g);
		g.translate(-dx, -dy);
	}

	@Override
	public int getWidth() {
		return TS;
	}

	@Override
	public int getHeight() {
		return TS;
	}
}