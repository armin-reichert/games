package de.amr.easy.maze.algorithms.wilson;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.iterators.traversals.ExpandingCircle;

/**
 * Wilson's algorithm where the vertices are selected from an expanding circle.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTExpandingCircle<Cell> extends WilsonUST<Cell> {

	public WilsonUSTExpandingCircle(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	protected Iterable<Cell> getCellSequence() {
		Cell center = grid.cell(GridPosition.CENTER);
		return new ExpandingCircle<>(grid, center, 1, Math.max(grid.numCols(), grid.numRows()));
	}

	@Override
	protected Cell modifyStartVertex(Cell start) {
		return grid.cell(GridPosition.CENTER);
	}
}
