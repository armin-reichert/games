package de.amr.games.pacman.controller;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;

public class PinkyBrain implements Brain<Ghost> {

	@Override
	public int recommendNextMoveDirection(Ghost pinky) {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(pinky.getMoveDirection())))
				.getAsInt();
	}
}
