package de.amr.games.pacman.core.board;

import static de.amr.easy.grid.impl.Top4.Top4;

/**
 * A tile of the board.
 * 
 * @author Armin Reichert
 */
public class Tile {

	private int row;
	private int col;

	public Tile(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public Tile(Tile other) {
		row = other.row;
		col = other.col;
	}

	public Tile translate(int dx, int dy) {
		col += dx;
		row += dy;
		return this;
	}

	public Tile neighbor(int dir) {
		return new Tile(row + Top4.dy(dir), col + Top4.dx(dir));
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public double distance(Tile tile) {
		int dx = tile.col - col, dy = tile.row - row;
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Tile[" + row + "|" + col + "]";
	}
}