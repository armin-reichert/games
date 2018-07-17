package de.amr.games.pacman.controller;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.PacMan;

/**
 * Blinky's behavior.
 */
public class ChaserMoveBehavior implements MoveBehavior {

	public final static Tile HOME = new Tile(13, 11);

	private final Maze maze;
	private final Ghost chaser;
	private final PacMan pacMan;
	private List<Tile> pathToTarget;

	public ChaserMoveBehavior(Maze maze, Ghost chaser, PacMan pacMan) {
		this.maze = maze;
		this.chaser = chaser;
		this.pacMan = pacMan;
		pathToTarget = Collections.emptyList();
	}

	@Override
	public int getNextMoveDirection() {
		int previousDir = chaser.getNextMoveDirection();
		switch (chaser.getState()) {
		case ATTACKING:
			return findPathDirection(pacMan.getMazePosition()).orElse(previousDir);
		case DEAD:
			return findPathDirection(HOME).orElse(previousDir);
		case FRIGHTENED:
		case SCATTERING:
		case STARRED:
		default:
			return previousDir;
		}
	}

	@Override
	public List<Tile> getPathToTarget() {
		return pathToTarget;
	}

	private OptionalInt findPathDirection(Tile target) {
		List<Tile> path = maze.findPath(chaser.getMazePosition(), target);
		return path.size() > 1 ? maze.direction(path.get(0), path.get(1)) : OptionalInt.empty();
	}
}