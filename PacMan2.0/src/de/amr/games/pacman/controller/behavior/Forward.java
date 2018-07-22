package de.amr.games.pacman.controller.behavior;

import java.util.function.Function;

import de.amr.games.pacman.ui.MazeMover;

public class Forward implements Function<MazeMover<?>, MoveData> {

	@Override
	public MoveData apply(MazeMover<?> mover) {
		MoveData result = new MoveData();
		result.dir = mover.getNextMoveDirection();
		return result;
	}
}