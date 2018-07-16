package de.amr.games.pacman.controller;

import java.util.List;
import java.util.OptionalInt;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.PacMan;

public class BlinkyBrain implements Brain {

	public final static Tile HOME = new Tile(13, 11);

	private final Ghost blinky;
	private final PacMan pacMan;
	private final Maze maze;

	public BlinkyBrain(Ghost blinky, PacMan pacMan, Maze maze) {
		this.blinky = blinky;
		this.pacMan = pacMan;
		this.maze = maze;
	}

	@Override
	public int recommendNextMoveDirection() {
		if (blinky.getState() == Ghost.State.ATTACKING) {
			return followPath(maze.findPath(blinky.getMazePosition(), pacMan.getMazePosition()))
					.orElse(blinky.getNextMoveDirection());
		}
		if (blinky.getState() == Ghost.State.DEAD) {
			return followPath(maze.findPath(blinky.getMazePosition(), HOME)).orElse(blinky.getNextMoveDirection());
		}
		return blinky.getNextMoveDirection();
	}

	private OptionalInt followPath(List<Integer> path) {
		return path.size() > 1 ? maze.direction(path.get(0), path.get(1)) : OptionalInt.empty();
	}
}