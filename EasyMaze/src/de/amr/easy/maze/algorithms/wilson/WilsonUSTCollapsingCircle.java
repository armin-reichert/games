package de.amr.easy.maze.algorithms.wilson;

import static de.amr.easy.grid.api.GridPosition.CENTER;
import static java.lang.Math.max;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.iterators.shapes.Circle;

/**
 * Wilson's algorithm where the vertices are selected from a collapsing circle.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTCollapsingCircle<Cell> extends WilsonUST<Cell> {

	public WilsonUSTCollapsingCircle(
			ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	public void accept(Cell start) {
		addCellToTree(start);
		Cell center = grid.cell(CENTER);
		for (int radius = max(grid.numRows(), grid.numCols()) - 1; radius >= 0; radius--) {
			for (Cell walkStart : new Circle<>(grid, center, radius)) {
				if (!isCellInTree(walkStart)) {
					loopErasedRandomWalk(walkStart);
				}
			}
		}
	}
}
