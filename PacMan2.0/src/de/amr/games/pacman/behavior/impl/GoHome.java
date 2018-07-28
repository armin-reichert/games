package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.ui.actor.MazeMover;

class GoHome implements RoutePlanner {

	@Override
	public Route computeRoute(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.path = mover.maze.findPath(mover.getTile(), mover.homeTile);
		result.dir = mover.maze.dirAlongPath(result.path).orElse(mover.getNextDir());
		return result;
	}
}