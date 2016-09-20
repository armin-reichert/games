package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;
import static de.amr.easy.graph.api.TraversalState.UNVISITED;
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
			if (neighbor != null && grid.getContent(neighbor) == UNVISITED) {
				grid.addEdge(new DefaultEdge<>(start, neighbor));
				accept(neighbor);
			}
		}
		grid.setContent(start, COMPLETED);
	}
}
