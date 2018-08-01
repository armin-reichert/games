package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.actor.Ghost;
import de.amr.games.pacman.ui.actor.PacMan;

public class PacManGhostCollisionEvent extends GameEvent {

	public final PacMan pacMan;
	public final Ghost ghost;

	public PacManGhostCollisionEvent(PacMan pacMan, Ghost ghost) {
		this.pacMan = pacMan;
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("GhostContact(%s)", ghost.getName());
	}
}