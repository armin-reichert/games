package de.amr.mazes.samples.maze;

import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.RecursiveDivision;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class RecursiveDivisionApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new RecursiveDivisionApp());
	}

	public RecursiveDivisionApp() {
		super("Recursive Division Maze", 600, 360, 2);
		work = () -> {
			while (true) {
				grid.fillAllEdges(); // does not fire events!
				for (Integer cell : grid.vertices()) {
					grid.setContent(cell, TraversalState.COMPLETED);
				}
				new RecursiveDivision<>(grid).accept(grid.cell(GridPosition.TOP_LEFT));
				new BFSAnimation(canvas, grid).runAnimation(grid.cell(GridPosition.TOP_LEFT));
				sleep(1000);
				clear();
			}
		};
	}
}