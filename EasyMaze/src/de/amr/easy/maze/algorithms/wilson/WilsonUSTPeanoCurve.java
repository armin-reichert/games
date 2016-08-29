package de.amr.easy.maze.algorithms.wilson;

import java.util.ArrayList;
import java.util.List;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.impl.CoordGrid;
import de.amr.easy.grid.iterators.traversals.PeanoCurve;
import de.amr.easy.maze.misc.Utils;

/**
 * Wilson's algorithm where the vertices are selected from a Peano-curve.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTPeanoCurve<Cell> extends WilsonUST<Cell> {

	private final List<Cell> path;

	public WilsonUSTPeanoCurve(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
		path = new ArrayList<>();
	}

	@Override
	protected Iterable<Cell> getCellSequence() {
		int nextPow3 = Utils.nextPow(3, Math.max(grid.numCols(), grid.numRows()));
		CoordGrid squareGrid = new CoordGrid(nextPow3, nextPow3);
		Integer cell = squareGrid.cell(0, squareGrid.numRows() - 1);
		addCellToPath(squareGrid.col(cell), squareGrid.row(cell));
		for (Direction d : new PeanoCurve(Utils.log(3, nextPow3))) {
			cell = squareGrid.neighbor(cell, d);
			addCellToPath(squareGrid.col(cell), squareGrid.row(cell));
		}
		return path;
	}

	private void addCellToPath(int x, int y) {
		if (grid.isValidCol(x) && grid.isValidRow(y)) {
			path.add(grid.cell(x, y));
		}
	}
}