package de.amr.games.pacman.controller.behavior.impl;

import de.amr.games.pacman.controller.behavior.MoveBehavior;
import de.amr.games.pacman.controller.behavior.Route;
import de.amr.games.pacman.ui.MazeMover;

class GoHome implements MoveBehavior {

	@Override
	public Route apply(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.path = mover.getMaze().findPath(mover.getTile(), mover.getHome());
		result.dir = mover.getMaze().dirAlongPath(result.path).orElse(mover.getNextMoveDirection());
		return result;
	}
}