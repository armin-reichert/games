package de.amr.games.pacman.controller.behavior.impl;

import de.amr.games.pacman.controller.behavior.MoveBehavior;
import de.amr.games.pacman.controller.behavior.Route;
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
	public Route apply(MazeMover<?> chaser) {
		RouteData result = new RouteData();
		result.path = chaser.getMaze().findPath(chaser.getTile(), victim.getTile());
		result.dir = chaser.getMaze().dirAlongPath(result.path).orElse(chaser.getNextMoveDirection());
		return result;
	}
}