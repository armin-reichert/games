package de.amr.games.pacman.controller;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;

public class ClydeBrain implements Brain {

	private final Ghost clyde;

	public ClydeBrain(Ghost clyde) {
		this.clyde = clyde;
	}

	@Override
	public int recommendNextMoveDirection() {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(clyde.getMoveDirection())))
				.getAsInt();
	}
}