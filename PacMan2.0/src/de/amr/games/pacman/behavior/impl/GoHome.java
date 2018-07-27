package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.ui.MazeMover;

class GoHome implements MoveBehavior {

	@Override
	public Route getRoute(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.path = mover.maze.findPath(mover.getTile(), mover.homeTile);
		result.dir = mover.maze.dirAlongPath(result.path).orElse(mover.getIntendedDirection());
		return result;
	}
}