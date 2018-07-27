package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.ui.MazeMover;

class Forward implements MoveBehavior {

	@Override
	public Route getRoute(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = mover.getNextMoveDirection();
		return result;
	}
}