package de.amr.games.pacman.behavior.impl;

import static de.amr.games.pacman.model.Maze.TOPOLOGY;
import static de.amr.games.pacman.model.TileContent.DOOR;
import static de.amr.games.pacman.model.TileContent.WALL;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.actor.MazeMover;

class Bounce implements RoutePlanner {

	@Override
	public Route computeRoute(MazeMover<?> bouncer) {
		RouteData route = new RouteData();
		route.dir = isReflected(bouncer) ? TOPOLOGY.inv(bouncer.getDir()) : bouncer.getDir();
		return route;
	}

	private boolean isReflected(MazeMover<?> bouncer) {
		Tile touchedTile = bouncer.computeTouchedTile(bouncer.getTile(), bouncer.getDir());
		if (touchedTile.equals(bouncer.getTile())) {
			return false;
		}
		char c = bouncer.maze.getContent(touchedTile);
		return c == WALL || c == DOOR;
	}
}