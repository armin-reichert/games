package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

class Bounce implements MoveBehavior {

	@Override
	public Route getRoute(MazeMover<?> bouncer) {
		RouteData result = new RouteData();
		result.dir = isReflected(bouncer) ? Maze.TOPOLOGY.inv(bouncer.getDirection())
				: bouncer.getDirection();
		return result;
	}

	private boolean isReflected(MazeMover<?> bouncer) {
		Tile touchedTile = bouncer.computeTouchedTile(bouncer.getTile(), bouncer.getDirection());
		if (touchedTile.equals(bouncer.getTile())) {
			return false;
		}
		char c = bouncer.maze.getContent(touchedTile);
		return c == Tile.WALL || c == Tile.DOOR;
	}
}