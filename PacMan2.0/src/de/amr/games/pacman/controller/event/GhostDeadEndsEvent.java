package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost;

public class GhostDeadEndsEvent implements GameEvent {

	public final Ghost ghost;

	public GhostDeadEndsEvent(Ghost ghost) {
		this.ghost = ghost;
	}
}