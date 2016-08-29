package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;
import static de.amr.easy.graph.api.TraversalState.VISITED;

import java.util.function.Consumer;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Maze generator using randomized recursive depth-first-search. Not suited for larger grids because
 * of stack overflow.
 * 
 * @author Armin Reichert
 */
public class RecursiveDFS<Cell> implements Consumer<Cell> {

	private final ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid;

	public RecursiveDFS(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		this.grid = grid;
	}

	@Override
	public void accept(Cell start) {
		grid.setContent(start, VISITED);
		for (Direction dir : Direction.randomOrder()) {
			Cell neighbor = grid.neighbor(start, dir);
			if (neighbor != null && grid.getContent(neighbor) == TraversalState.UNVISITED) {
				grid.addEdge(new DefaultEdge<Cell>(start, neighbor));
				accept(neighbor);
			}
		}
		// Direction.valuesShuffled().stream().map(dir -> grid.getNeighbor(start, dir))
		// .filter(Objects::nonNull)
		// .filter(neighbor -> grid.getContent(neighbor) == TraversalState.UNVISITED)
		// .forEach(unvisitedNeighbor -> {
		// grid.addEdge(new DefaultEdge<Cell>(start, unvisitedNeighbor));
		// accept(unvisitedNeighbor);
		// });
		grid.setContent(start, COMPLETED);
	}
}
