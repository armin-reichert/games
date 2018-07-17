package de.amr.games.pacman.controller;

import static de.amr.easy.util.StreamUtils.randomElement;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Ghost;

/**
 * Pinky's behaviour.
 */
public class AmbusherMoveBehaviour implements MoveBehaviour {

	private final Ghost ambusher;

	public AmbusherMoveBehaviour(Ghost ambusher) {
		this.ambusher = ambusher;
	}

	@Override
	public int getNextMoveDirection() {
		return randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(ambusher.getMoveDirection())))
				.getAsInt();
	}
}
