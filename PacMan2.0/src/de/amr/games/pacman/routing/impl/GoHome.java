package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.routing.Route;
import de.amr.games.pacman.routing.RoutePlanner;

class GoHome implements RoutePlanner {

	@Override
	public Route computeRoute(MazeMover<?> mover) {
		RouteData route = new RouteData();
		route.path = mover.maze.findPath(mover.getTile(), mover.homeTile);
		route.dir = mover.maze.alongPath(route.path).orElse(mover.getNextDir());
		return route;
	}
}