package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.actor.PacMan;

public class PacManGainsPowerEvent extends GameEvent {

	public final PacMan pacMan;
	public final int ticks;

	public PacManGainsPowerEvent(PacMan pacMan, int ticks) {
		this.pacMan = pacMan;
		this.ticks = ticks;
	}

}
