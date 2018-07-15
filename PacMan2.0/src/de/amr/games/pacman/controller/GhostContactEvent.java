package de.amr.games.pacman.controller;

import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.PacMan;

public class GhostContactEvent implements GameEvent {

	public final PacMan pacMan;
	public final Ghost ghost;
	public final int col;
	public final int row;

	public GhostContactEvent(PacMan pacMan, Ghost ghost, int col, int row) {
		this.pacMan = pacMan;
		this.ghost = ghost;
		this.col = col;
		this.row = row;
	}
}
