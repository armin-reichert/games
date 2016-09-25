package de.amr.easy.maze.algorithms.wilson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.iterators.IteratorFactory;
import de.amr.easy.grid.iterators.shapes.Rectangle;
import de.amr.easy.grid.iterators.traversals.ExpandingRectangle;

/**
 * Wilson's algorithm where the vertices are selected from a sequence of nested rectangles.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTNestedRectangles<Cell> extends WilsonUST<Cell> {

	public WilsonUSTNestedRectangles(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	protected Cell modifyStartVertex(Cell start) {
		return grid.cell(0, 0);
	}

	@Override
	protected Iterable<Cell> getCellSequence() {
		return new Iterable<Cell>() {

			@Override
			public Iterator<Cell> iterator() {
				Rectangle<Cell> firstCell = new Rectangle<>(grid, grid.cell(0, 0), 1, 1);
				List<Iterator<Cell>> expRects = new ArrayList<>();
				int rate = grid.numCols();
				while (rate > 1) {
					expRects.add(expandingRectangle(firstCell, rate).iterator());
					rate /= 2;
				}
				Iterator<Cell>[] expRectsArray = expRects.toArray(new Iterator[expRects.size()]);

				Rectangle<Cell> firstColumn = new Rectangle<>(grid, grid.cell(0, 0), 1, grid.numRows());
				ExpandingRectangle<Cell> sweep = new ExpandingRectangle<>(firstColumn);
				sweep.setExpandHorizontally(true);
				sweep.setExpandVertically(false);
				sweep.setExpansionRate(1);
				sweep.setMaxExpansion(grid.numCols());

				return IteratorFactory.sequence(IteratorFactory.sequence(expRectsArray), sweep.iterator());
			}
		};
	}

	private ExpandingRectangle<Cell> expandingRectangle(Rectangle<Cell> startRectangle, int rate) {
		ExpandingRectangle<Cell> r = new ExpandingRectangle<>(startRectangle);
		r.setExpandHorizontally(true);
		r.setExpandVertically(true);
		r.setExpansionRate(rate);
		r.setMaxExpansion(grid.numCols() - startRectangle.getWidth());
		return r;
	}
}
