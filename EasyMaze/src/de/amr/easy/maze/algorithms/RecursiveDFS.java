package de.amr.easy.maze.algorithms;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;
import static de.amr.easy.graph.api.TraversalState.UNVISITED;
import static de.amr.easy.graph.api.TraversalState.VISITED;

import java.util.function.Consumer;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Maze generator using randomized recursive depth-first-search. Not suited for larger grids because
 * of stack overflow.
 * 
 * @author Armin Reichert
 */
public class RecursiveDFS<Cell> implements Consumer<Cell> {

	private final ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid;
	private Cell neighbor;

	public RecursiveDFS(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		this.grid = grid;
	}

	@Override
	public void accept(Cell cell) {
		grid.setContent(cell, VISITED);
		while ((neighbor = grid.randomNeighbor(cell, c -> grid.getContent(c) == UNVISITED)) != null) {
			grid.addEdge(new DefaultEdge<>(cell, neighbor));
			accept(neighbor);
		}
		grid.setContent(cell, COMPLETED);
	}
}
