package de.amr.easy.maze.algorithms.wilson;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.iterators.shapes.Rectangle;
import de.amr.easy.grid.iterators.traversals.ExpandingRectangle;

/**
 * Wilson's algorithm where the vertices are selected from several circles in turn.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTExpandingRectangle<Cell> extends WilsonUST<Cell> {

	public WilsonUSTExpandingRectangle(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	protected Iterable<Cell> getCellSequence() {
		Rectangle<Cell> startRectangle = new Rectangle<Cell>(grid, grid.cell(0, 0), 1, 1);
		ExpandingRectangle<Cell> expRect = new ExpandingRectangle<>(startRectangle);
		expRect.setExpandHorizontally(true);
		expRect.setExpandVertically(true);
		expRect.setMaxExpansion(grid.numCols() - 1);
		return expRect;
	}

	@Override
	protected Cell modifyStartVertex(Cell start) {
		return grid.cell(0, 0);
	}
}
