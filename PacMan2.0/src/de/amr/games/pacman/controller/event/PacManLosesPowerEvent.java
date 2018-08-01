package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.actor.PacMan;

public class PacManLosesPowerEvent extends GameEvent {

	public final PacMan pacMan;

	public PacManLosesPowerEvent(PacMan pacMan) {
		this.pacMan = pacMan;
	}

}
