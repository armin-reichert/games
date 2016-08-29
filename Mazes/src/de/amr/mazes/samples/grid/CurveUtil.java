package de.amr.mazes.samples.grid;

import static de.amr.easy.graph.api.TraversalState.COMPLETED;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.impl.DefaultEdge;
import de.amr.easy.grid.api.Direction;
import de.amr.easy.grid.impl.ObservableCoordDataGrid;

public class CurveUtil {

	public static void buildGrid(ObservableCoordDataGrid<TraversalState> grid, Iterable<Direction> curve,
			Integer startCell, Runnable onEdgeAdded) {
		DefaultEdge<Integer> dummyEdge = new DefaultEdge<>(null, null);
		Integer cell = startCell;
		grid.setContent(cell, COMPLETED);
		for (Direction dir : curve) {
			Integer next = grid.neighbor(cell, dir);
			dummyEdge.setEither(cell);
			dummyEdge.setOther(next);
			grid.addEdge(dummyEdge);
			onEdgeAdded.run();
			cell = next;
			grid.setContent(cell, COMPLETED);
		}
	}
}
