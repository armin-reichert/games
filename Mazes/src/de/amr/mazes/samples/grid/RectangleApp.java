package de.amr.mazes.samples.grid;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.grid.iterators.shapes.Rectangle;
import de.amr.easy.grid.iterators.traversals.ExpandingRectangle;

public class RectangleApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new RectangleApp());
	}

	public RectangleApp() {
		super("Rectangles", 400, 300, 2);
		work = () -> {
			canvas.setDelay(3);
			Rectangle<Integer> startRectangle = new Rectangle<Integer>(grid, grid.cell(0, 0), 1, 1);
			ExpandingRectangle<Integer> expRect = new ExpandingRectangle<Integer>(startRectangle);
			expRect.setExpandHorizontally(true);
			expRect.setExpandVertically(true);
			expRect.setExpansionRate(1);
			expRect.setMaxExpansion(grid.numCols());
			for (Integer cell : expRect) {
				grid.setContent(cell, TraversalState.COMPLETED);
			}
		};
	}
}
