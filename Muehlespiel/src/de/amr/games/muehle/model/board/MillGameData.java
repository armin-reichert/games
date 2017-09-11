package de.amr.games.muehle.model.board;

/**
 * Data model of the mill game.
 * 
 * @author Armin Reichert
 */
public class MillGameData {

	public final Board board;
	public int whiteStonesPlaced;
	public int blackStonesPlaced;

	public MillGameData() {
		this.board = new Board();
	}
}