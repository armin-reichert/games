package de.amr.games.pacman.controller.behavior;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.ui.MazeMover;

public class FollowKeyboard implements MoveBehavior {

	private final int[] nesw;

	public FollowKeyboard(MazeMover<?> mover, int... nesw) {
		if (nesw.length != 4) {
			throw new IllegalArgumentException("Must specify 4 keyboard codes for steering");
		}
		this.nesw = nesw;
	}

	@Override
	public MoveData apply(MazeMover<?> mover) {
		MoveData result = new MoveData();
		result.dir = mover.getNextMoveDirection();
		if (Keyboard.keyDown(nesw[0])) {
			result.dir = Top4.N;
		}
		if (Keyboard.keyDown(nesw[1])) {
			result.dir = Top4.E;
		}
		if (Keyboard.keyDown(nesw[2])) {
			result.dir = Top4.S;
		}
		if (Keyboard.keyDown(nesw[3])) {
			result.dir = Top4.W;
		}
		return result;
	}
}