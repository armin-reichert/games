package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Ambusher behavior.
 */
public class AmbushTarget implements MoveBehavior {

	private final Maze maze;
	private final MazeMover<?> ambusher;
	private final MazeMover<?> target;

	public AmbushTarget(Maze maze, MazeMover<?> ambusher, MazeMover<?> target) {
		this.maze = maze;
		this.ambusher = ambusher;
		this.target = target;
	}

	@Override
	public int getNextMoveDirection() {
		return ambusher.getNextMoveDirection(); // TODO
	}
}