package de.amr.games.pacman.controller.behavior;

import java.util.List;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Chasing a refugee through the maze.
 */
public class Chase implements MoveBehavior {

	private final Maze maze;
	private final MazeMover<?> chaser;
	private final MazeMover<?> refugee;
	private List<Tile> targetPath;

	public Chase(MazeMover<?> chaser, MazeMover<?> refugee) {
		this.maze = chaser.getMaze();
		this.chaser = chaser;
		this.refugee = refugee;
	}

	@Override
	public int getNextMoveDirection() {
		targetPath = maze.findPath(chaser.getTile(), refugee.getTile());
		return maze.dirAlongPath(targetPath).orElse(chaser.getNextMoveDirection());
	}

	@Override
	public List<Tile> getTargetPath() {
		return targetPath;
	}
}