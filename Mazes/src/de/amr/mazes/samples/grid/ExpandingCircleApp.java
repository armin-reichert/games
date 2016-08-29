package de.amr.mazes.samples.grid;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.grid.iterators.traversals.ExpandingCircle;

public class ExpandingCircleApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new ExpandingCircleApp());
	}

	public ExpandingCircleApp() {
		super("Expanding Circle", 300, 150, 4);
		work = () -> {
			setDelay(5);
			Integer start = grid.cell(GridPosition.CENTER);
			int radius = Math.max(grid.numCols(), grid.numRows());
			ExpandingCircle<Integer> circle = new ExpandingCircle<>(grid, start, 0, radius);
			for (Integer cell : circle) {
				grid.setContent(cell, TraversalState.COMPLETED);
			}
		};
	}
}
