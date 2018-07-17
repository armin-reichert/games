package de.amr.games.pacman.controller;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;

/**
 * Inky's behaviour.
 */
public class MoodyMoveBehavior implements MoveBehavior {

	private final Ghost ghost;

	public MoodyMoveBehavior(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public int getNextMoveDirection() {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(ghost.getMoveDirection())))
				.getAsInt();
	}
}