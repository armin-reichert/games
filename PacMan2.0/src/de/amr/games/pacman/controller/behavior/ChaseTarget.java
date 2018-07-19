package de.amr.games.pacman.controller.behavior;

import java.util.List;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Chase target behavior.
 */
public class ChaseTarget implements MoveBehavior {

	private final Maze maze;
	private final MazeMover<?> chaser;
	private final MazeMover<?> target;

	public ChaseTarget(Maze maze, MazeMover<?> chaser, MazeMover<?> target) {
		this.maze = maze;
		this.chaser = chaser;
		this.target = target;
	}

	@Override
	public int getNextMoveDirection() {
		List<Tile> path = maze.findPath(chaser.getTile(), target.getTile());
		int dir = maze.alongPath(path).orElse(chaser.getNextMoveDirection());
		return dir;
	}
}