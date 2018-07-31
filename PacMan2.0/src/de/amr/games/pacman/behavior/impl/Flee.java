package de.amr.games.pacman.behavior.impl;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

import de.amr.games.pacman.behavior.Route;
import de.amr.games.pacman.behavior.RoutePlanner;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.actor.MazeMover;

class Flee implements RoutePlanner {

	private final MazeMover<?> chaser;

	public Flee(MazeMover<?> chaser) {
		this.chaser = chaser;
	}

	@Override
	public Route computeRoute(MazeMover<?> refugee) {
		RouteData route = new RouteData();
		route.dir = refugee.getNextDir();
		Maze maze = chaser.maze;
		OptionalInt towardsChaser = maze.alongPath(maze.findPath(refugee.getTile(), chaser.getTile()));
		if (towardsChaser.isPresent()) {
			int dir = towardsChaser.getAsInt();
			for (int d : Arrays.asList(Maze.TOPOLOGY.inv(dir), Maze.TOPOLOGY.right(dir),
					Maze.TOPOLOGY.left(dir))) {
				Optional<Tile> neighbor = maze.neighborTile(refugee.getTile(), d);
				if (neighbor.isPresent() && maze.hasAdjacentTile(refugee.getTile(), neighbor.get())) {
					route.dir = d;
					break;
				}
			}
		}
		return route;
	}
}