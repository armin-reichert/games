package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Chasing a refugee through the maze.
 */
class Chase implements MoveBehavior {

	private final MazeMover<?> victim;

	public Chase(MazeMover<?> victim) {
		this.victim = victim;
	}

	@Override
	public Route getRoute(MazeMover<?> chaser) {
		RouteData result = new RouteData();
		result.path = chaser.maze.findPath(chaser.getTile(), victim.getTile());
		result.dir = chaser.maze.dirAlongPath(result.path).orElse(chaser.getIntendedDirection());
		return result;
	}
}