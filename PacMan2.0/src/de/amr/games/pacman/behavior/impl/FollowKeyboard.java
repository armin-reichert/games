package de.amr.games.pacman.behavior.impl;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.ui.MazeMover;

class FollowKeyboard implements MoveBehavior {

	private final int[] nesw;

	public FollowKeyboard(int... nesw) {
		if (nesw.length != 4) {
			throw new IllegalArgumentException("Must specify 4 keyboard codes for steering");
		}
		this.nesw = nesw;
	}

	@Override
	public Route getRoute(MazeMover<?> mover) {
		RouteData result = new RouteData();
		result.dir = mover.getIntendedDirection();
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