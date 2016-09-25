package de.amr.easy.maze.algorithms.wilson;

import static de.amr.easy.grid.api.GridPosition.CENTER;
import static de.amr.easy.grid.iterators.IteratorFactory.parallel;
import static de.amr.easy.grid.iterators.IteratorFactory.sequence;

import java.util.Iterator;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.ObservableDataGrid2D;
import de.amr.easy.grid.iterators.traversals.ExpandingCircle;

/**
 * Wilson's algorithm where grid cells are selected from five expanding circles.
 * 
 * @author Armin Reichert
 */
public class WilsonUSTExpandingCircles<Cell> extends WilsonUST<Cell> {

	public WilsonUSTExpandingCircles(ObservableDataGrid2D<Cell, DefaultEdge<Cell>, TraversalState> grid) {
		super(grid);
	}

	@Override
	protected Cell modifyStartVertex(Cell start) {
		return grid.cell(CENTER);
	}

	@Override
	protected Iterable<Cell> getCellSequence() {
		return new Iterable<Cell>() {

			@Override
			public Iterator<Cell> iterator() {
				int w = grid.numCols(), h = grid.numRows(), r = Math.max(w / 2, h / 2);
				/*@formatter:off*/
				return sequence(
					// expand 4 circles in parallel to certain size	
					parallel(
						expandingCircle(w / 4, h / 4, 1, r / 4),
						expandingCircle(3 * w / 4, h / 4, 1, r / 4),
						expandingCircle(w / 4, 3 * h / 4, 1, r / 4), 
						expandingCircle(3 * w / 4, 3 * h / 4, 1, r / 4)
					),
					// expand 5th circle to half its size
					expandingCircle(w / 2, h / 2, 1, r / 2),
					// expand first 4 circles to final size
					parallel(
						expandingCircle(w / 4, h / 4, r / 4, r / 2),
						expandingCircle(3 * w / 4, h / 4, r / 4, r / 2), 
						expandingCircle(w / 4, 3 * h / 4, r / 4, r / 2),
						expandingCircle(3 * w / 4, 3 * h / 4, r / 4, r / 2)
					),
					// expand 5th circle to final size
					expandingCircle(w / 2, h / 2, r / 2, 2 * r)
				);
				/*@formatter:on*/
			}
		};
	}

	private Iterator<Cell> expandingCircle(int centerX, int centerY, int rmin, int rmax) {
		return new ExpandingCircle<>(grid, grid.cell(centerX, centerY), rmin, rmax).iterator();
	}
}
