package de.amr.easy.maze.algorithms.wilson;

import static de.amr.easy.grid.api.GridPosition.CENTER;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.iterators.traversals.Spiral;

/**
 * Wilson's algorithm where the vertices are selected from an expanding spiral.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTExpandingSpiral<Cell> extends WilsonUST<Cell> {

	public WilsonUSTExpandingSpiral(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	protected Iterable<Cell> getCellSequence() {
		Cell center = grid.cell(CENTER);
		return new Spiral<>(grid, center);
	}

	@Override
	protected Cell modifyStartVertex(Cell start) {
		return grid.cell(CENTER);
	}
}
