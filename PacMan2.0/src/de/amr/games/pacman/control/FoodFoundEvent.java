package de.amr.games.pacman.control;

import de.amr.games.pacman.entities.PacMan;

public class FoodFoundEvent implements GameEvent {

	public final PacMan pacMan;
	public final char food;
	public final int col;
	public final int row;

	public FoodFoundEvent(PacMan pacMan, int col, int row, char food) {
		this.pacMan = pacMan;
		this.col = col;
		this.row = row;
		this.food = food;
	}
}
