package de.amr.games.pacman.controller.behavior;

import static de.amr.easy.util.StreamUtils.randomElement;

import java.util.function.Function;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeMover;

/**
 * Inky's behaviour.
 */
public class Moody implements Function<MazeMover<?>, MoveData> {

	@Override
	public MoveData apply(MazeMover<?> mover) {
		MoveData result = new MoveData();
		result.dir = randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(mover.getMoveDirection())))
				.getAsInt();
		return result;
	}
}