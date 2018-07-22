package de.amr.games.pacman.controller.behavior.impl;

import de.amr.games.pacman.controller.behavior.MoveBehavior;
import de.amr.games.pacman.controller.behavior.Route;
import de.amr.games.pacman.ui.MazeMover;

class Forward implements MoveBehavior {

	@Override
	public Route apply(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = mover.getNextMoveDirection();
		return result;
	}
}