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
	public int getNextMoveDirection(MazeMover<?> mover) {
		if (Keyboard.keyDown(nesw[0])) {
			return Top4.N;
		}
		if (Keyboard.keyDown(nesw[1])) {
			return Top4.E;
		}
		if (Keyboard.keyDown(nesw[2])) {
			return Top4.S;
		}
		if (Keyboard.keyDown(nesw[3])) {
			return Top4.W;
		}
		return mover.getNextMoveDirection();
	}
}