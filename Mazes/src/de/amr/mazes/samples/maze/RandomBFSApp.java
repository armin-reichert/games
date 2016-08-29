package de.amr.mazes.samples.maze;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.RandomBFS;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class RandomBFSApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new RandomBFSApp());
	}

	public RandomBFSApp() {
		super("Randomized Bread-First-Traversal Maze", 600, 360, 2);
		work = () -> {
			Integer startCell = grid.cell(GridPosition.CENTER);
			while (true) {
				new RandomBFS<>(grid).accept(startCell);
				new BFSAnimation(canvas, grid).runAnimation(startCell);
				sleep(1000);
				clear();
			}
		};
	}
}