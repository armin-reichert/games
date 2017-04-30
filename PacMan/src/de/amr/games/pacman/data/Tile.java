package de.amr.games.pacman.data;

import static java.lang.Math.round;

import java.awt.geom.Point2D;

/**
 * A tile of the board.
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