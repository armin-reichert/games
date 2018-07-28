package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.actor.MazeMover;

class Bounce implements RoutePlanner {

	@Override
	public Route computeRoute(MazeMover<?> bouncer) {
		RouteData result = new RouteData();
		result.dir = isReflected(bouncer) ? Maze.TOPOLOGY.inv(bouncer.getDir()) : bouncer.getDir();
		return result;
	}

	private boolean isReflected(MazeMover<?> bouncer) {
		Tile touchedTile = bouncer.computeTouchedTile(bouncer.getTile(), bouncer.getDir());
		if (touchedTile.equals(bouncer.getTile())) {
			return false;
		}
		char c = bouncer.maze.getContent(touchedTile);
		return c == Tile.WALL || c == Tile.DOOR;
	}
}