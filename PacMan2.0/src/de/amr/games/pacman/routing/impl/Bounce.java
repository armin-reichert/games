package de.amr.games.pacman.routing.impl;

import static de.amr.games.pacman.model.Content.DOOR;
import static de.amr.games.pacman.model.Content.WALL;
import static de.amr.games.pacman.model.Maze.TOPOLOGY;

import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.routing.Route;
import de.amr.games.pacman.routing.RoutePlanner;
import de.amr.games.pacman.ui.actor.MazeMover;

class Bounce implements RoutePlanner {

	@Override
	public Route computeRoute(MazeMover<?> bouncer) {
		RouteData route = new RouteData();
		route.dir = isReflected(bouncer) ? TOPOLOGY.inv(bouncer.getDir()) : bouncer.getDir();
		return route;
	}

	private boolean isReflected(MazeMover<?> bouncer) {
		Tile nextTile = bouncer.computeNextTile(bouncer.getTile(), bouncer.getDir());
		if (nextTile.equals(bouncer.getTile())) {
			return false;
		}
		char c = bouncer.maze.getContent(nextTile);
		return c == WALL || c == DOOR;
	}
}