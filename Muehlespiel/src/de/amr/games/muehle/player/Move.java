package de.amr.games.muehle.player;

/**
 * A move or jump.
 * 
 * @author Armin Reichert
 */
public class Move {

	public int from;
	public int to;

	public Move() {
		this(-1, -1);
	}

	public Move(int from, int to) {
		this.from = from;
		this.to = to;
	}
}