package de.amr.games.pacman.data;

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
	
	private static final Top4 top4 = new Top4();

	public Tile(float row, float col) {
		x = col;
		y = row;
	}

	public Tile(Tile other) {
		x = other.x;
		y = other.y;
	}

	public void translate(float dx, float dy) {
		x += dx;
		y += dy;
	}
	
	public Tile neighbor(int dir) {
		return new Tile(y + top4.dy(dir), x + top4.dx(dir));
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