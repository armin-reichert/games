package de.amr.games.pacman.data;

public enum TileContent {

	Empty(' '), Wall('#'), Door('D'), GhostHouse('G'), Pellet('.'), Energizer('O'), Tunnel('T'), Wormhole('W');

	private TileContent(char ch) {
		this.ch = ch;
	}

	private final char ch;

	public char toChar() {
		return ch;
	}
};
