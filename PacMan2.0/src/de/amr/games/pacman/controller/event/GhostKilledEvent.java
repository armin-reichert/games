package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost;

public class GhostKilledEvent extends GameEvent {

	public final Ghost ghost;

	public GhostKilledEvent(Ghost ghost) {
		this.ghost = ghost;
	}
}