package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.ui.actor.MazeMover;

class GoHome implements RoutePlanner {

	@Override
	public Route computeRoute(MazeMover<?> mover) {
		RouteData route = new RouteData();
		route.path = mover.maze.findPath(mover.getTile(), mover.homeTile);
		route.dir = mover.maze.alongPath(route.path).orElse(mover.getNextDir());
		return route;
	}
}