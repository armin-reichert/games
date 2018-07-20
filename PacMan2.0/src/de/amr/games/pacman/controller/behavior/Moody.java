package de.amr.games.pacman.controller.behavior;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost.State;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Inky's behaviour.
 */
public class Moody implements MoveBehavior {

	private final MazeMover<State> ghost;

	public Moody(MazeMover<State> ghost) {
		this.ghost = ghost;
	}

	@Override
	public int getNextMoveDirection() {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(ghost.getMoveDirection())))
				.getAsInt();
	}
}