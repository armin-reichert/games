package de.amr.games.pacman.core.board;

import static java.lang.Math.round;

import java.awt.geom.Point2D;

import de.amr.easy.grid.impl.Top4;

/**
 * A tile of the board. Tile coordinates are floats because entities can be positioned between
 * tiles.
 * 
 * @author Armin Reichert
 */
public class Tile extends Point2D.Float {

	public Tile(float row, float col) {
		x = col;
		y = row;
	}

	public Tile(Tile other) {
		x = other.x;
		y = other.y;
	}

	public Tile translate(float dx, float dy) {
		x += dx;
		y += dy;
		return this;
	}

	public Tile neighbor(int dir) {
		return new Tile(y + Top4.INSTANCE.dy(dir), x + Top4.INSTANCE.dx(dir));
	}

	public int getRow() {
		return round(y);
	}

	public int getCol() {
		return round(x);
	}

	@Override
	public String toString() {
		return "Tile[" + getRow() + "|" + getCol() + "]";
	}
}