package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.actor.PacMan;

public class PacManLostPowerEvent extends GameEvent {

	public final PacMan pacMan;

	public PacManLostPowerEvent(PacMan pacMan) {
		this.pacMan = pacMan;
	}

}
