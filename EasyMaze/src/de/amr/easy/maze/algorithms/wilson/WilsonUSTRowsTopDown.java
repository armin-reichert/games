package de.amr.easy.maze.algorithms.wilson;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;

/**
 * Wilson's algorithm where the vertices are selected row-wise.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTRowsTopDown<Cell> extends WilsonUST<Cell> {

	public WilsonUSTRowsTopDown(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	public void accept(Cell start) {
		addCellToTree(start);
		for (int y = 0; y < grid.numRows(); ++y) {
			for (int x = 0; x < grid.numCols(); ++x) {
				Cell walkStart = grid.cell(x, y);
				if (!isCellInTree(walkStart)) {
					loopErasedRandomWalk(walkStart);
				}
			}
		}
	}
}
