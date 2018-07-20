package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeMover;

public class GoHome implements MoveBehavior {

	private final Maze maze;
	private final MazeMover<?> mover;

	public GoHome(MazeMover<?> mover) {
		this.maze = mover.getMaze();
		this.mover = mover;
	}

	@Override
	public int getNextMoveDirection() {
		return maze.dirAlongPath(maze.findPath(mover.getTile(), mover.getHome())).orElse(mover.getNextMoveDirection());
	}
}
