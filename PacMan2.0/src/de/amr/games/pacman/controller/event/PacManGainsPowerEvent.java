package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.actor.PacMan;

public class PacManGainsPowerEvent extends GameEvent {

	public final PacMan pacMan;

	public PacManGainsPowerEvent(PacMan pacMan) {
		this.pacMan = pacMan;
	}

}
