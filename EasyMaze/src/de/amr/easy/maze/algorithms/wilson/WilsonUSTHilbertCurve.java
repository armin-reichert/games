package de.amr.easy.maze.algorithms.wilson;

import static de.amr.easy.maze.misc.Utils.log;
import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.List;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.impl.RawGrid;
import de.amr.easy.grid.iterators.traversals.HilbertCurve;
import de.amr.easy.maze.misc.Utils;

/**
 * Wilson's algorithm where the vertices are selected from a Hilbert curve.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTHilbertCurve<Cell> extends WilsonUST<Cell> {

	public WilsonUSTHilbertCurve(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	protected Iterable<Cell> getCellSequence() {
		int nextPow2 = Utils.nextPow(2, max(grid.numCols(), grid.numRows()));
		RawGrid squareGrid = new RawGrid(nextPow2, nextPow2);
		List<Cell> path = new ArrayList<>();
		path.add(grid.cell(0, 0));
		HilbertCurve hilbertCurve = new HilbertCurve(log(2, nextPow2), Direction.W, Direction.N,
				Direction.E, Direction.S);
		Integer cell = squareGrid.cell(0, 0);
		for (Direction d : hilbertCurve) {
			cell = squareGrid.neighbor(cell, d);
			int x = squareGrid.col(cell), y = squareGrid.row(cell);
			if (grid.isValidCol(x) && grid.isValidRow(y)) {
				path.add(grid.cell(x, y));
			}
		}
		return path;
	}
}
