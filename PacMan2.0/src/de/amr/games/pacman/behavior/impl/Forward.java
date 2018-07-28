package de.amr.games.pacman.behavior.impl;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.ui.actor.MazeMover;

class Forward implements RoutePlanner {

	@Override
	public Route computeRoute(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = mover.getNextDir();
		return result;
	}
}