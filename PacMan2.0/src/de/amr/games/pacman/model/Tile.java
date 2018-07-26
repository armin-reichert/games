package de.amr.games.pacman.model;

public class Tile {

	// Structure
	public static final char EMPTY = ' ';
	public static final char WALL = '#';
	public static final char DOOR = 'D';
	public static final char TUNNEL = 'T';
	public static final char WORMHOLE = 'W';

	// Position markers
	public static final char POS_INFO = '$';
	public static final char POS_PACMAN = 'O';
	public static final char POS_BLINKY = 'B';
	public static final char POS_INKY = 'I';
	public static final char POS_PINKY = 'P';
	public static final char POS_CLYDE = 'C';

	// Food
	public static final char PELLET = '.';
	public static final char ENERGIZER = '*';

	public static boolean isFood(char c) {
		return c == PELLET || c == ENERGIZER;
	}

	public final int col;
	public final int row;

	public Tile(int col, int row) {
		this.col = col;
		this.row = row;
	}

	@Override
	public int hashCode() {
		int sum = col + row;
		return sum * (sum + 1) / 2 + col;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Tile) {
			Tile other = (Tile) obj;
			return col == other.col && row == other.row;
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return String.format("(%d,%d)", col, row);
	}
}