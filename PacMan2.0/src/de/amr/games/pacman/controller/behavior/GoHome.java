package de.amr.games.pacman.controller.behavior;

import de.amr.games.pacman.ui.MazeMover;

public class GoHome implements MoveBehavior {

	private final MazeMover<?> mover;

	public GoHome(MazeMover<?> mover) {
		this.mover = mover;
	}

	@Override
	public int getNextMoveDirection() {
		return mover.getMaze().dirAlongPath(mover.getMaze().findPath(mover.getTile(), mover.getHome()))
				.orElse(mover.getNextMoveDirection());
	}
}