package de.amr.games.pacman.behavior.impl;

import java.util.Optional;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.actor.MazeMover;

/**
 * Ambush the victim in the maze.
 */
class Ambush implements RoutePlanner {

	private final MazeMover<?> victim;

	public Ambush(MazeMover<?> victim) {
		this.victim = victim;
	}

	@Override
	public Route computeRoute(MazeMover<?> ambusher) {
		RouteData result = new RouteData();
		Maze maze = victim.maze;
		Optional<Tile> fourAhead = ahead(4, victim);
		if (fourAhead.isPresent() && maze.getContent(fourAhead.get()) != Tile.WALL) {
			result.path = maze.findPath(ambusher.getTile(), fourAhead.get());
		} else {
			result.path = maze.findPath(ambusher.getTile(), victim.getTile());
		}
		result.dir = maze.dirAlongPath(result.path).orElse(ambusher.getNextDir());
		return result;
	}

	private Optional<Tile> ahead(int n, MazeMover<?> refugee) {
		Tile current = refugee.getTile();
		for (int i = 0; i < n; ++i) {
			Optional<Tile> next = refugee.maze.neighborTile(current, refugee.getDir());
			if (next.isPresent()) {
				current = next.get();
			}
		}
		return Optional.of(current);
	}
}