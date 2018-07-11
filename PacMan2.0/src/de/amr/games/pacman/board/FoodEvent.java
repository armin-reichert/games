package de.amr.games.pacman.board;


public class FoodEvent {
	
	public final int col;
	public final int row;
	public final char food;
	
	
	public FoodEvent(int col, int row, char food) {
		this.col = col;
		this.row = row;
		this.food = food;
	}

}
