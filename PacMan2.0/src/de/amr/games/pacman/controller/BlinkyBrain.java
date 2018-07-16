package de.amr.games.pacman.controller;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;

public class BlinkyBrain implements Brain<Ghost> {

	@Override
	public int recommendNextMoveDirection(Ghost blinky) {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(blinky.getMoveDirection())))
				.getAsInt();
	}
}
