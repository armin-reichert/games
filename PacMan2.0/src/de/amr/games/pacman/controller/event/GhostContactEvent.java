package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Ghost.State;
import de.amr.games.pacman.ui.MazeMover;

public class GhostContactEvent implements GameEvent {

	public final MazeMover<State> ghost;
	public final Tile tile;

	public GhostContactEvent(MazeMover<State> ghost, Tile tile) {
		this.ghost = ghost;
		this.tile = tile;
	}
}