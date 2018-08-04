package de.amr.games.pacman.routing.impl;

import static de.amr.games.pacman.model.Content.WALL;

import java.util.Optional;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.Route;
import de.amr.games.pacman.routing.RoutePlanner;
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
		RouteData route = new RouteData();
		if (victim.isTeleporting()) {
			route.dir = ambusher.getNextDir();
			return route;
		}
		Maze maze = victim.maze;
		Optional<Tile> fourAhead = ahead(4, victim);
		if (fourAhead.isPresent() && maze.getContent(fourAhead.get()) != WALL) {
			route.path = maze.findPath(ambusher.getTile(), fourAhead.get());
		} else {
			route.path = maze.findPath(ambusher.getTile(), victim.getTile());
		}
		route.dir = maze.alongPath(route.path).orElse(ambusher.getNextDir());
		return route;
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