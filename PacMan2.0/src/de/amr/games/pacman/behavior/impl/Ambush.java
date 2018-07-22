package de.amr.games.pacman.behavior.impl;

import java.util.Optional;

import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Ambush the victim in the maze.
 */
class Ambush implements MoveBehavior {

	private final MazeMover<?> victim;

	public Ambush(MazeMover<?> victim) {
		this.victim = victim;
	}

	@Override
	public Route apply(MazeMover<?> ambusher) {
		RouteData result = new RouteData();
		Maze maze = victim.getMaze();
		Optional<Tile> fourAhead = ahead(4, victim);
		if (fourAhead.isPresent() && maze.getContent(fourAhead.get()) != Tile.WALL) {
			result.path = maze.findPath(ambusher.getTile(), fourAhead.get());
		} else {
			result.path = maze.findPath(ambusher.getTile(), victim.getTile());
		}
		result.dir = maze.dirAlongPath(result.path).orElse(ambusher.getNextMoveDirection());
		return result;
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