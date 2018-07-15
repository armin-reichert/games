package de.amr.games.pacman.controller;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.ui.MazeMover;

public class KeyboardSteering implements Consumer<MazeMover<?>> {

	@Override
	public void accept(MazeMover<?> mover) {
		if (Keyboard.keyDown(KeyEvent.VK_LEFT)) {
			mover.setNextMoveDirection(Top4.W);
		} else if (Keyboard.keyDown(KeyEvent.VK_RIGHT)) {
			mover.setNextMoveDirection(Top4.E);
		} else if (Keyboard.keyDown(KeyEvent.VK_DOWN)) {
			mover.setNextMoveDirection(Top4.S);
		} else if (Keyboard.keyDown(KeyEvent.VK_UP)) {
			mover.setNextMoveDirection(Top4.N);
		}
	}
}