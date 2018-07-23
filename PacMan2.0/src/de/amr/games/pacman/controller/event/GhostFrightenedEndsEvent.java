package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost.State;
import de.amr.games.pacman.ui.MazeMover;

public class GhostFrightenedEndsEvent extends GameEvent {

	public final MazeMover<State> ghost;

	public GhostFrightenedEndsEvent(MazeMover<State> ghost) {
		this.ghost = ghost;
	}
}