package de.amr.games.pacman.data;

/**
 * Enum type for the content in a tile.
 * 
 * @author Armin Reichert
 */
public enum TileContent {

	None(' '), Wall('#'), Door('D'), GhostHouse('G'), Pellet('.'), Energizer('O'), Tunnel('T'), Wormhole('W');

	private TileContent(char ch) {
		this.ch = ch;
	}

	private final char ch;

	public char toChar() {
		return ch;
	}
};
