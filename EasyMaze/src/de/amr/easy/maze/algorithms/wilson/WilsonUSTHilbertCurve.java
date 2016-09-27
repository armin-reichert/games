package de.amr.easy.maze.algorithms.wilson;

import static de.amr.easy.grid.api.Direction.E;
import static de.amr.easy.grid.api.Direction.N;
import static de.amr.easy.grid.api.Direction.S;
import static de.amr.easy.grid.api.Direction.W;
import static de.amr.easy.maze.misc.Utils.log;
import static de.amr.easy.maze.misc.Utils.nextPow;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.List;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.impl.RawGrid;
import de.amr.easy.grid.iterators.traversals.HilbertCurve;

/**
 * Wilson's algorithm where the vertices are selected from a Hilbert curve.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTHilbertCurve extends WilsonUST {

	public WilsonUSTHilbertCurve(ObservableDataGrid2D<Integer, DefaultEdge<Integer>, TraversalState> grid) {
		super(grid);
	}

	@Override
	protected Iterable<Integer> getCellSequence() {
		// Hilbert curve need a square grid, so create one
		int n = nextPow(2, max(grid.numCols(), grid.numRows()));
		RawGrid square = new RawGrid(n, n);
		HilbertCurve hilbert = new HilbertCurve(log(2, n), W, N, E, S);
		List<Integer> path = new ArrayList<>();
		path.add(grid.cell(0, 0));
		// Traverse the intersection of the square grid cells with the original grid
		Integer cell = square.cell(0, 0);
		for (Direction dir : hilbert) {
			// As the Hilbert curve never leaves the square grid, the neighbor is never NULL
			cell = square.neighbor(cell, dir);
			// Check if next cell on Hilbert curve is inside original grid:
			int col = square.col(cell), row = square.row(cell);
			if (grid.isValidCol(col) && grid.isValidRow(row)) {
				path.add(grid.cell(col, row));
			}
		}
		return path;
	}
}
