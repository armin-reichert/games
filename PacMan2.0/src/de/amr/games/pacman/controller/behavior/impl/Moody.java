package de.amr.games.pacman.controller.behavior.impl;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.controller.behavior.MoveBehavior;
import de.amr.games.pacman.controller.behavior.Route;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Inky's behaviour.
 */
class Moody implements MoveBehavior {

	@Override
	public Route apply(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(mover.getMoveDirection())))
				.getAsInt();
		return result;
	}
}