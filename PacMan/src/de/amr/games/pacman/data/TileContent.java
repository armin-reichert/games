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

	public static TileContent valueOf(char c) {
		for (TileContent content : values()) {
			if (c == content.ch) {
				return content;
			}
		}
		throw new IllegalArgumentException("Unknown tile content '" + c + "'");
	}
};
