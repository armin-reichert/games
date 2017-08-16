package de.amr.games.muehle.board;

/**
 * Stone color (white or black).
 */
public enum StoneColor {
	WHITE, BLACK;

	public StoneColor other() {
		return this == WHITE ? BLACK : WHITE;
	}
}