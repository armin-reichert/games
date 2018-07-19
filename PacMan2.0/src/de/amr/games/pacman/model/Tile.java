package de.amr.games.pacman.model;

public class Tile {

	public static final char EMPTY = ' ';
	public static final char WALL = '#';
	public static final char DOOR = 'D';
	public static final char TUNNEL = 'T';
	public static final char WORMHOLE = 'W';

	// Food
	public static final char PELLET = '.';
	public static final char ENERGIZER = 'O';

	// Bonus
	public static final char BONUS_CHERRIES = '1';
	public static final char BONUS_STRAWBERRY = '2';
	public static final char BONUS_PEACH = '3';
	public static final char BONUS_APPLE = '4';
	public static final char BONUS_GRAPES = '5';
	public static final char BONUS_GALAXIAN = '6';
	public static final char BONUS_BELL = '7';
	public static final char BONUS_KEY = '8';

	// Position markers
	public static final char BONUS = '$';
	public static final char PACMAN = '@';
	public static final char BLINKY = 'B';
	public static final char INKY = 'I';
	public static final char PINKY = 'P';
	public static final char CLYDE = 'C';

	public static boolean isFood(char c) {
		return c == PELLET || c == ENERGIZER;
	}

	public static boolean isBonus(char c) {
		return c == BONUS_APPLE || c == BONUS_BELL || c == BONUS_CHERRIES || c == BONUS_GALAXIAN || c == BONUS_GRAPES
				|| c == BONUS_KEY || c == BONUS_PEACH || c == BONUS_STRAWBERRY;
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