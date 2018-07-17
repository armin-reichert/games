package de.amr.games.pacman.controller.behavior;

import static de.amr.easy.util.StreamUtils.randomElement;

import java.util.Arrays;
import java.util.List;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Ghost;

/**
 * Pinky's behaviour.
 */
public class AmbusherMoveBehavior implements MoveBehavior {

	private final Ghost ambusher;
	private List<Tile> pathToTarget;

	public AmbusherMoveBehavior(Ghost ambusher) {
		this.ambusher = ambusher;
	}

	@Override
	public int getNextMoveDirection() {
		int next = randomElement(Maze.TOPOLOGY.dirs().filter(dir -> dir != Maze.TOPOLOGY.inv(ambusher.getMoveDirection())))
				.getAsInt();
		pathToTarget = Arrays.asList(ambusher.getMazePosition());
		return next;
	}

	@Override
	public List<Tile> getPathToTarget() {
		return pathToTarget;
	}
}
