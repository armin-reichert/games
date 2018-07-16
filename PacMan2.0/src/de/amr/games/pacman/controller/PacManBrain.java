package de.amr.games.pacman.controller;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.ui.MazeMover;
import de.amr.games.pacman.ui.PacMan.State;

public class PacManBrain implements Brain {

	private final MazeMover<State> pacMan;

	public PacManBrain(MazeMover<State> pacMan) {
		this.pacMan = pacMan;
	}

	@Override
	public int recommendNextMoveDirection() {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			return Top4.W;
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			return Top4.E;
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			return Top4.S;
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			return Top4.N;
		} else {
			return pacMan.getNextMoveDirection();
		}
	}
}