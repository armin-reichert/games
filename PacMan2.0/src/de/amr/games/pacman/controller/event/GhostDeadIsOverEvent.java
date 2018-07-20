package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost;

public class GhostDeadIsOverEvent implements GameEvent {

	public final Ghost ghost;

	public GhostDeadIsOverEvent(Ghost ghost) {
		this.ghost = ghost;
	}
}