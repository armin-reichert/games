package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.ui.MazeMover;

public class DoNothing implements MoveBehavior {

	private MazeMover<?> mover;

	public DoNothing(MazeMover<?> mover) {
		this.mover = mover;
	}

	@Override
	public int getNextMoveDirection() {
		return mover.getNextMoveDirection();
	}
}