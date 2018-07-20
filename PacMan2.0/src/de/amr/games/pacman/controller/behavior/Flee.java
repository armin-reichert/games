package de.amr.games.pacman.controller.behavior;

import java.util.Arrays;
import java.util.OptionalInt;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeMover;

public class Flee implements MoveBehavior {

	private final Maze maze;
	private final MazeMover<?> refugee;
	private final MazeMover<?> chaser;

	public Flee(MazeMover<?> refugee, MazeMover<?> chaser) {
		this.maze = refugee.getMaze();
		this.refugee = refugee;
		this.chaser = chaser;
	}

	@Override
	public int getNextMoveDirection() {
		OptionalInt towardsChaser = maze.dirAlongPath(maze.findPath(refugee.getTile(), chaser.getTile()));
		if (towardsChaser.isPresent()) {
			int dir = towardsChaser.getAsInt();
			for (int d : Arrays.asList(Maze.TOPOLOGY.inv(dir), Maze.TOPOLOGY.right(dir), Maze.TOPOLOGY.left(dir))) {
				OptionalInt neighbor = maze.neighbor(maze.cell(refugee.getTile()), d);
				if (neighbor.isPresent() && maze.adjacent(maze.cell(refugee.getTile()), neighbor.getAsInt())) {
					return d;
				}
			}
		}
		return refugee.getNextMoveDirection();
	}
}