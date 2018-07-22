package de.amr.games.pacman.controller.behavior.impl;

import de.amr.games.pacman.controller.behavior.MoveBehavior;
import de.amr.games.pacman.controller.behavior.Route;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

public class Bounce implements MoveBehavior {

	@Override
	public Route apply(MazeMover<?> bouncer) {
		RouteData result = new RouteData();
		result.dir = isReflected(bouncer) ? Maze.TOPOLOGY.inv(bouncer.getMoveDirection()) : bouncer.getMoveDirection();
		return result;
	}

	private boolean isReflected(MazeMover<?> bouncer) {
		Tile touchedTile = bouncer.computeTouchedTile(bouncer.getTile(), bouncer.getMoveDirection());
		if (touchedTile.equals(bouncer.getTile())) {
			return false;
		}
		char c = bouncer.getMaze().getContent(touchedTile);
		return c == Tile.WALL || c == Tile.DOOR;
	}
}