package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan.State;

public class GhostContactEvent implements GameEvent {

	public final MazeMover<State> pacMan;
	public final Ghost ghost;
	public final int col;
	public final int row;

	public GhostContactEvent(MazeMover<State> pacMan, Ghost ghost, int col, int row) {
		this.pacMan = pacMan;
		this.ghost = ghost;
		this.col = col;
		this.row = row;
	}
}
