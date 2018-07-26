package de.amr.games.pacman.behavior.impl;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import de.amr.games.pacman.behavior.MoveBehavior;
import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.MazeMover;

class Flee implements MoveBehavior {

	private final MazeMover<?> chaser;

	public Flee(MazeMover<?> chaser) {
		this.chaser = chaser;
	}

	@Override
	public Route apply(MazeMover<?> refugee) {
		RouteData result = new RouteData();
		result.dir = refugee.getNextMoveDirection();
		Maze maze = chaser.getMaze();
		OptionalInt towardsChaser = maze
				.dirAlongPath(maze.findPath(refugee.getTile(), chaser.getTile()));
		if (towardsChaser.isPresent()) {
			int dir = towardsChaser.getAsInt();
			for (int d : Arrays.asList(Maze.TOPOLOGY.inv(dir), Maze.TOPOLOGY.right(dir),
					Maze.TOPOLOGY.left(dir))) {
				Optional<Tile> neighbor = maze.neighbor(refugee.getTile(), d);
				if (neighbor.isPresent() && maze.adjacent(refugee.getTile(), neighbor.get())) {
					result.dir = d;
					break;
				}
			}
		}
		return result;
	}
}