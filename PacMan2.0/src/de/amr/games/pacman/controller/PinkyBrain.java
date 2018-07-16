package de.amr.games.pacman.controller;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;

public class PinkyBrain implements Brain {

	private final Ghost pinky;

	public PinkyBrain(Ghost pinky) {
		this.pinky = pinky;
	}

	@Override
	public int recommendNextMoveDirection() {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(pinky.getMoveDirection())))
				.getAsInt();
	}
}
