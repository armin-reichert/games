package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.ui.MazeMover;

/**
 * Chasing a refugee through the maze.
 */
public class Chase implements MoveBehavior {

	private final MazeMover<?> refugee;

	public Chase(MazeMover<?> refugee) {
		this.refugee = refugee;
	}

	@Override
	public MoveData apply(MazeMover<?> chaser) {
		MoveData result = new MoveData();
		result.path = chaser.getMaze().findPath(chaser.getTile(), refugee.getTile());
		result.dir = chaser.getMaze().dirAlongPath(result.path).orElse(chaser.getNextMoveDirection());
		return result;
	}
}