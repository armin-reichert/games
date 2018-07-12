package de.amr.games.pacman.entities;

public class GhostEvent {

	public final Ghost ghost;
	public final int col;
	public final int row;

	public GhostEvent(Ghost ghost, int col, int row) {
		this.ghost = ghost;
		this.col = col;
		this.row = row;
	}
}
