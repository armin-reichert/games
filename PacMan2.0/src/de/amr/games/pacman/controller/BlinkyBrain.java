package de.amr.games.pacman.controller;

import static de.amr.games.pacman.controller.GameController.whenDebugging;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import de.amr.easy.graph.impl.traversal.AStarTraversal;
import de.amr.easy.grid.impl.Top4;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.model.Tile;
import de.amr.games.pacman.ui.Ghost;
import de.amr.games.pacman.ui.PacMan;

public class BlinkyBrain implements Brain<Ghost> {

	public final static Tile HOME = new Tile(13, 11);

	private final PacMan pacMan;
	private final Maze maze;

	public BlinkyBrain(PacMan pacMan, Maze maze) {
		this.pacMan = pacMan;
		this.maze = maze;
	}

	@Override
	public int recommendNextMoveDirection(Ghost blinky) {
		if (blinky.getState() == Ghost.State.ATTACKING) {
			List<Integer> path = findPathToPacMan(blinky);
			OptionalInt recommendedDir = getDirection(path);
			if (recommendedDir.isPresent()) {
				int dir = recommendedDir.getAsInt();
				whenDebugging(() -> System.out.println("Blinky will move " + dirName(dir)));
				return dir;
			}
		}

		else if (blinky.getState() == Ghost.State.DEAD) {
			List<Integer> path = findPathHome(blinky);
			OptionalInt recommendedDir = getDirection(path);
			if (recommendedDir.isPresent()) {
				int dir = recommendedDir.getAsInt();
				whenDebugging(() -> System.out.println("Blinky will move " + dirName(dir)));
				return dir;
			}
		}

		return blinky.getNextMoveDirection();
	}

	private OptionalInt getDirection(List<Integer> path) {
		return path.size() > 1 ? maze.direction(path.get(0), path.get(1)) : OptionalInt.empty();
	}

	private List<Integer> findPathHome(Ghost blinky) {
		AStarTraversal<?> pathfinder = new AStarTraversal<>(maze, maze::manhattan);
		int source = maze.cell(blinky.col(), blinky.row());
		int target = maze.cell(HOME.col, HOME.row);
		pathfinder.traverseGraph(source, target);
		return pathfinder.path(target);
	}

	private List<Integer> findPathToPacMan(Ghost blinky) {
		AStarTraversal<?> pathfinder = new AStarTraversal<>(maze, maze::manhattan);
		int source = maze.cell(blinky.col(), blinky.row());
		int target = maze.cell(pacMan.col(), pacMan.row());
		pathfinder.traverseGraph(source, target);
		List<Integer> path = pathfinder.path(target);
		whenDebugging(() -> {
			System.out.println(
					path.stream().map(v -> "(" + maze.col(v) + "," + maze.row(v) + ")").collect(Collectors.joining("-")));
		});
		return path;
	}

	private String dirName(int dir) {
		switch (dir) {
		case Top4.N:
			return "North";
		case Top4.E:
			return "East";
		case Top4.S:
			return "South";
		case Top4.W:
			return "West";
		}
		throw new IllegalArgumentException();
	}
}
