package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost;

public class GhostFrightenedEndsEvent implements GameEvent {

	public final Ghost ghost;

	public GhostFrightenedEndsEvent(Ghost ghost) {
		this.ghost = ghost;
	}
}