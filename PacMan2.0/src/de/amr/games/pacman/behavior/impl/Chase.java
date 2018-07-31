package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.ui.actor.MazeMover;

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
		if (victim.isOutsideMaze()) {
			route.dir = chaser.getNextDir();
			return route;
		}
		route.path = chaser.maze.findPath(chaser.getTile(), victim.getTile());
		route.dir = chaser.maze.alongPath(route.path).orElse(chaser.getNextDir());
		return route;
	}
}