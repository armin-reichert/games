package de.amr.games.pacman.controller;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan.State;

public class KeyboardSteering implements MoveBehavior {

	private final MazeMover<State> mover;

	public KeyboardSteering(MazeMover<State> mover) {
		this.mover = mover;
	}

	@Override
	public int getNextMoveDirection() {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			return Top4.W;
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			return Top4.E;
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			return Top4.S;
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			return Top4.N;
		} else {
			return mover.getNextMoveDirection();
		}
	}
}