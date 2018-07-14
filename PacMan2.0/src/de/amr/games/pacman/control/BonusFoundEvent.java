package de.amr.games.pacman.control;

import de.amr.games.pacman.entities.PacMan;

public class BonusFoundEvent implements GameEvent {

	public final PacMan pacMan;
	public final char bonus;
	public final int col;
	public final int row;

	public BonusFoundEvent(PacMan pacMan, int col, int row, char bonus) {
		this.pacMan = pacMan;
		this.col = col;
		this.row = row;
		this.bonus = bonus;
	}

}
