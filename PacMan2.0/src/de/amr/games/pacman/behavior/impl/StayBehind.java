package de.amr.games.pacman.behavior.impl;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Clyde's behaviour.
 */
class StayBehind implements MoveBehavior {

	@Override
	public Route apply(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(mover.getMoveDirection())))
				.getAsInt();
		return result;
	}
}