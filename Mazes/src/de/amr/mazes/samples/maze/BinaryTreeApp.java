package de.amr.mazes.samples.maze;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.BinaryTree;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class BinaryTreeApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new BinaryTreeApp());
	}

	public BinaryTreeApp() {
		super("Binary Tree Maze", 300, 180, 4);
		work = () -> {
			Integer startCell = grid.cell(GridPosition.TOP_LEFT);
			while (true) {
				new BinaryTree<>(grid).accept(startCell);
				new BFSAnimation(canvas, grid).runAnimation(startCell);
				sleep(1000);
				clear();
			}
		};
	}
}
