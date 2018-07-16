package de.amr.games.pacman.controller;

import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan.State;

public class BonusFoundEvent implements GameEvent {

	public final MazeMover<State> pacMan;
	public final char bonus;
	public final int col;
	public final int row;

	public BonusFoundEvent(MazeMover<State> pacMan, int col, int row, char bonus) {
		this.pacMan = pacMan;
		this.col = col;
		this.row = row;
		this.bonus = bonus;
	}

}
