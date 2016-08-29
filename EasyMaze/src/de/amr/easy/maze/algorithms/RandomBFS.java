package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;
import static de.amr.easy.graph.api.TraversalState.VISITED;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Maze generator using a randomized breadth-first-traversal.
 * 
 * @author Armin Reichert
 */
public class RandomBFS<Cell> implements Consumer<Cell> {

	private final ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid;

	public RandomBFS(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		this.grid = grid;
	}

	@Override
	public void accept(Cell start) {
		final Random rnd = new Random();
		final Set<Cell> mazeCells = new HashSet<>();
		final List<Cell> frontier = new LinkedList<>();
		
		mazeCells.add(start);
		frontier.add(start);
		grid.setContent(start, VISITED);
		while (!frontier.isEmpty()) {
			Cell cell = frontier.remove(rnd.nextInt(frontier.size()));
			/*@formatter:off*/
			Stream.of(Direction.randomOrder())
				.map(dir -> grid.neighbor(cell, dir))
				.filter(Objects::nonNull)
				.filter(neighbor -> !mazeCells.contains(neighbor))
				.forEach(neighborNotInMaze -> {
					grid.addEdge(new DefaultEdge<Cell>(cell, neighborNotInMaze));
					grid.setContent(neighborNotInMaze, VISITED);
					mazeCells.add(neighborNotInMaze);
					frontier.add(neighborNotInMaze);
				});
			/*@formatter:on*/
			grid.setContent(cell, COMPLETED);
		}
	}
}
