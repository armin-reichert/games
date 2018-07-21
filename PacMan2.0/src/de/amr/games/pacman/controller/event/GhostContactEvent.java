package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost;

public class GhostContactEvent implements GameEvent {

	public final Ghost ghost;

	public GhostContactEvent(Ghost ghost) {
		this.ghost = ghost;
	}
}