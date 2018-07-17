package de.amr.games.pacman.controller;

import static de.amr.games.pacman.controller.GameController.debug;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import de.amr.easy.grid.impl.Top4;
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
		pathToTarget = maze.findPath(chaser.getMazePosition(), target);
		debug(() -> {
			String pathString = pathToTarget.stream().map(Tile::toString).collect(Collectors.joining("-"));
			System.out.println("Chaser path: " + pathString);
		});
		if (pathToTarget.size() < 2) {
			return OptionalInt.empty();
		}
		OptionalInt pathDirection = maze.direction(pathToTarget.get(0), pathToTarget.get(1));
		debug(() -> pathDirection.ifPresent(dir -> System.out.println("Chase direction: " + name(dir))));
		return pathDirection;
	}

	private String name(int dir) {
		if (dir == Top4.N)
			return "N";
		if (dir == Top4.E)
			return "E";
		if (dir == Top4.S)
			return "S";
		if (dir == Top4.W)
			return "W";
		return "";
	}
}