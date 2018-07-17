package de.amr.games.pacman.controller;

import java.util.List;
import java.util.OptionalInt;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.PacMan;

/**
 * Blinky's behaviour.
 */
public class ChaserMoveBehaviour implements MoveBehaviour {

	public final static Tile HOME = new Tile(13, 11);

	private final Maze maze;
	private final Ghost chaser;
	private final PacMan pacMan;

	public ChaserMoveBehaviour(Maze maze, Ghost chaser, PacMan pacMan) {
		this.maze = maze;
		this.chaser = chaser;
		this.pacMan = pacMan;
	}

	@Override
	public int getNextMoveDirection() {
		int previousDir = chaser.getNextMoveDirection();
		switch (chaser.getState()) {
		case ATTACKING:
			return followPath(pacMan.getMazePosition()).orElse(previousDir);
		case DEAD:
			return followPath(HOME).orElse(previousDir);
		case FRIGHTENED:
		case SCATTERING:
		case STARRED:
		default:
			return previousDir;
		}
	}

	private OptionalInt followPath(Tile target) {
		List<Integer> path = maze.findPath(chaser.getMazePosition(), target);
		return path.size() > 1 ? maze.direction(path.get(0), path.get(1)) : OptionalInt.empty();
	}
}