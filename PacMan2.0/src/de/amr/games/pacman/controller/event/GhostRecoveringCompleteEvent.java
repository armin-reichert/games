package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost;

public class GhostRecoveringCompleteEvent implements GameEvent {

	public final Ghost ghost;

	public GhostRecoveringCompleteEvent(Ghost ghost) {
		this.ghost = ghost;
	}
}