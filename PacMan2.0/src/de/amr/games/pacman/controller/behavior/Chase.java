package de.amr.games.pacman.controller.behavior;

import java.util.List;
import java.util.Optional;

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
	private Tile targetTile;
	private List<Tile> targetPath;

	public Chase(Maze maze, MazeMover<?> chaser, MazeMover<?> refugee) {
		this.maze = maze;
		this.chaser = chaser;
		this.refugee = refugee;
	}

	@Override
	public int getNextMoveDirection() {
		targetTile = refugee.getTile();
		targetPath = maze.findPath(chaser.getTile(), targetTile);
		return maze.dirAlongPath(targetPath).orElse(chaser.getNextMoveDirection());
	}
	
	@Override
	public Optional<Tile> getTargetTile() {
		return Optional.ofNullable(targetTile);
	}
	
	@Override
	public List<Tile> getTargetPath() {
		return targetPath;
	}
}