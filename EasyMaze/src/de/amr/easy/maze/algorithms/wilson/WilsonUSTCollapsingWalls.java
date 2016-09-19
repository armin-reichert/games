package de.amr.easy.maze.algorithms.wilson;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.iterators.traversals.CollapsingWalls;

/**
 * Wilson's algorithm where the vertices are selected column-wise left-to-right.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTCollapsingWalls<Cell> extends WilsonUST<Cell> {

	public WilsonUSTCollapsingWalls(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	protected Iterable<Cell> getCellSequence() {
		return new CollapsingWalls<>(grid);
	}
}
