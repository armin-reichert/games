package de.amr.games.pacman.routing.impl;

import de.amr.games.pacman.routing.Route;
import de.amr.games.pacman.routing.RoutePlanner;
import de.amr.games.pacman.ui.actor.MazeMover;

class Forward implements RoutePlanner {

	@Override
	public Route computeRoute(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = mover.getNextDir();
		return result;
	}
}