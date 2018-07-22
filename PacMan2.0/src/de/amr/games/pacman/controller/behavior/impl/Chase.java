package de.amr.games.pacman.controller.behavior.impl;

import de.amr.games.pacman.controller.behavior.MoveBehavior;
import de.amr.games.pacman.controller.behavior.Route;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Chasing a refugee through the maze.
 */
public class Chase implements MoveBehavior {

	private final MazeMover<?> refugee;

	public Chase(MazeMover<?> refugee) {
		this.refugee = refugee;
	}

	@Override
	public Route apply(MazeMover<?> chaser) {
		RouteData result = new RouteData();
		result.path = chaser.getMaze().findPath(chaser.getTile(), refugee.getTile());
		result.dir = chaser.getMaze().dirAlongPath(result.path).orElse(chaser.getNextMoveDirection());
		return result;
	}
}