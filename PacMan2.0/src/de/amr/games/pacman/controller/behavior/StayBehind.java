package de.amr.games.pacman.controller.behavior;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Clyde's behaviour.
 */
public class StayBehind implements MoveBehavior {

	@Override
	public MoveData apply(MazeMover<?> mover) {
		MoveData result = new MoveData();
		result.dir = randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(mover.getMoveDirection())))
				.getAsInt();
		return result;
	}
}