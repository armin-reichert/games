package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost.State;
import de.amr.games.pacman.ui.MazeMover;

public class GhostDeadIsOverEvent extends GameEvent {

	public final MazeMover<State> ghost;

	public GhostDeadIsOverEvent(MazeMover<State> ghost) {
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return String.format("GhostDeadIsOver(%s)", ghost.getName());
	}
}