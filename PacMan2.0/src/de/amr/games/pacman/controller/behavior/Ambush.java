package de.amr.games.pacman.controller.behavior;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Ambushing a refugee in the maze.
 */
public class Ambush implements MoveBehavior {

	private final MazeMover<?> ambusher;
	private final MazeMover<?> refugee;
	private List<Tile> targetPath;

	public Ambush(MazeMover<?> ambusher, MazeMover<?> refugee) {
		this.ambusher = ambusher;
		this.refugee = refugee;
		this.targetPath = Collections.emptyList();
	}

	@Override
	public int getNextMoveDirection() {
		Maze maze = refugee.getMaze();
		Optional<Tile> fourAhead = ahead(4, refugee);
		if (fourAhead.isPresent() && maze.getContent(fourAhead.get()) != Tile.WALL) {
			targetPath = maze.findPath(ambusher.getTile(), fourAhead.get());
		} else {
			targetPath = maze.findPath(ambusher.getTile(), refugee.getTile());
		}
		return maze.dirAlongPath(targetPath).orElse(ambusher.getNextMoveDirection());
	}

	@Override
	public List<Tile> getTargetPath() {
		return targetPath;
	}

	private Optional<Tile> ahead(int n, MazeMover<?> refugee) {
		Tile current = refugee.getTile();
		for (int i = 0; i < n; ++i) {
			Optional<Tile> next = refugee.getMaze().neighbor(current, refugee.getMoveDirection());
			if (next.isPresent()) {
				current = next.get();
			}
		}
		return Optional.of(current);
	}
}