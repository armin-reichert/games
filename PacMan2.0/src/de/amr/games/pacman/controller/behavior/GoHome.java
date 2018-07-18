package de.amr.games.pacman.controller.behavior;

import java.util.List;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

public class GoHome implements MoveBehavior {

	private final Maze maze;
	private final MazeMover<?> mover;
	private final Tile homeTile;

	public GoHome(Maze maze, MazeMover<?> mover, Tile homeTile) {
		this.maze = maze;
		this.mover = mover;
		this.homeTile = homeTile;
	}

	@Override
	public int getNextMoveDirection() {
		List<Tile> path = maze.findPath(mover.getMazePosition(), homeTile);
		return maze.alongPath(path).orElse(mover.getNextMoveDirection());
	}
}
