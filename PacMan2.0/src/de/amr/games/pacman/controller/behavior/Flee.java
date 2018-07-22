package de.amr.games.pacman.controller.behavior;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.Function;

import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeMover;

public class Flee implements Function<MazeMover<?>, MoveData> {

	private final MazeMover<?> chaser;

	public Flee(MazeMover<?> chaser) {
		this.chaser = chaser;
	}

	@Override
	public MoveData apply(MazeMover<?> refugee) {
		MoveData result = new MoveData();
		result.dir = refugee.getNextMoveDirection();
		Maze maze = chaser.getMaze();
		OptionalInt towardsChaser = maze.dirAlongPath(maze.findPath(refugee.getTile(), chaser.getTile()));
		if (towardsChaser.isPresent()) {
			int dir = towardsChaser.getAsInt();
			for (int d : Arrays.asList(Maze.TOPOLOGY.inv(dir), Maze.TOPOLOGY.right(dir), Maze.TOPOLOGY.left(dir))) {
				OptionalInt neighbor = maze.neighbor(maze.cell(refugee.getTile()), d);
				if (neighbor.isPresent() && maze.adjacent(maze.cell(refugee.getTile()), neighbor.getAsInt())) {
					result.dir = d;
					break;
				}
			}
		}
		return result;
	}
}