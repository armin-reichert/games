package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost;

public class GhostContactEvent extends GameEvent {

	public final Ghost ghost;

	public GhostContactEvent(Ghost ghost) {
		this.ghost = ghost;
	}
	
	@Override
	public String toString() {
		return String.format("GhostContact(%s)", ghost.getName());
	}
}