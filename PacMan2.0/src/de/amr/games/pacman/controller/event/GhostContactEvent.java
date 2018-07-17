package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Ghost;

public class GhostContactEvent implements GameEvent {

	public final Ghost ghost;
	public final Tile tile;

	public GhostContactEvent(Ghost ghost, Tile tile) {
		this.ghost = ghost;
		this.tile = tile;
	}
}