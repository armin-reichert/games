package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.ui.MazeMover;

public class Forward implements MoveBehavior {

	@Override
	public MoveData apply(MazeMover<?> mover) {
		MoveData result = new MoveData();
		result.dir = mover.getNextMoveDirection();
		return result;
	}
}