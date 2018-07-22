package de.amr.games.pacman.controller.behavior;

import java.util.function.Function;

import de.amr.games.pacman.ui.MazeMover;

public class GoHome implements Function<MazeMover<?>, MoveData> {

	@Override
	public MoveData apply(MazeMover<?> mover) {
		MoveData result = new MoveData();
		result.path = mover.getMaze().findPath(mover.getTile(), mover.getHome());
		result.dir = mover.getMaze().dirAlongPath(result.path).orElse(mover.getNextMoveDirection());
		return result;
	}
}