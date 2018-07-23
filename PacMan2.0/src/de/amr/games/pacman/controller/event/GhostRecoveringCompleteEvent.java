package de.amr.games.pacman.controller.event;

import de.amr.games.pacman.ui.Ghost.State;
import de.amr.games.pacman.ui.MazeMover;

public class GhostRecoveringCompleteEvent extends GameEvent {

	public final MazeMover<State> ghost;

	public GhostRecoveringCompleteEvent(MazeMover<State> ghost) {
		this.ghost = ghost;
	}
}