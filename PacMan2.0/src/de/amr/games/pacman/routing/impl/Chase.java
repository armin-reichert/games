package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.actor.MazeMover;
import de.amr.games.pacman.routing.Route;
import de.amr.games.pacman.routing.RoutePlanner;

/**
 * Chasing a refugee through the maze.
 */
class Chase implements RoutePlanner {

	private final MazeMover<?> victim;

	public Chase(MazeMover<?> victim) {
		this.victim = victim;
	}

	@Override
	public Route computeRoute(MazeMover<?> chaser) {
		RouteData route = new RouteData();
		if (victim.isTeleporting()) {
			route.dir = chaser.getNextDir();
			return route;
		}
		route.path = chaser.maze.findPath(chaser.getTile(), victim.getTile());
		route.dir = chaser.maze.alongPath(route.path).orElse(chaser.getNextDir());
		return route;
	}
}