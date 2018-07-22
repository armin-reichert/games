package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.ui.MazeMover;

public class DoNothing implements MoveBehavior {

	@Override
	public int getNextMoveDirection(MazeMover<?> mover) {
		return mover.getNextMoveDirection();
	}
}