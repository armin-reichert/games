package de.amr.games.pacman.controller;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;

/**
 * Clyde's behaviour.
 */
public class LackingBehindMoveBehavior implements MoveBehavior {

	private final Ghost ghost;

	public LackingBehindMoveBehavior(Ghost ghost) {
		this.ghost = ghost;
	}

	@Override
	public int getNextMoveDirection() {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(ghost.getMoveDirection())))
				.getAsInt();
	}
}