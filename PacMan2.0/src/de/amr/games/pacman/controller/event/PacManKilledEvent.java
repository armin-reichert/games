package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.actor.Ghost;

public class PacManKilledEvent extends GameEvent {

	public final Ghost ghost;

	public PacManKilledEvent(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("PacManKilledEvent(%s)", ghost.getName());
	}
}
