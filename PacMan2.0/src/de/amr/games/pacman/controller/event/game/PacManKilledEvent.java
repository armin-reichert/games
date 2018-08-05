package de.amr.games.pacman.controller.event.game;

import de.amr.games.pacman.actor.Ghost;
import de.amr.games.pacman.controller.event.core.GameEvent;

public class PacManKilledEvent extends GameEvent {

	public final Ghost killer;

	public PacManKilledEvent(Ghost ghost) {
		this.killer = ghost;
	}

	@Override
	public String toString() {
		return String.format("PacManKilledEvent(%s)", killer.getName());
	}
}
