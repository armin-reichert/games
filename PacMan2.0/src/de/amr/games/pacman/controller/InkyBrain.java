package de.amr.games.pacman.controller;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;

public class InkyBrain implements Brain {

	private final Ghost inky;

	public InkyBrain(Ghost inky) {
		this.inky = inky;
	}

	@Override
	public int recommendNextMoveDirection() {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(inky.getMoveDirection())))
				.getAsInt();
	}

}
