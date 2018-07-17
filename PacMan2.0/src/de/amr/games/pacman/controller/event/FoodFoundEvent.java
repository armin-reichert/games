package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan.State;

public class FoodFoundEvent implements GameEvent {

	public final MazeMover<State> pacMan;
	public final char food;
	public final int col;
	public final int row;

	public FoodFoundEvent(MazeMover<State> pacMan, int col, int row, char food) {
		this.pacMan = pacMan;
		this.col = col;
		this.row = row;
		this.food = food;
	}
}
