package de.amr.mazes.samples.maze;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.PrimMST;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class PrimMSTApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new PrimMSTApp());
	}

	public PrimMSTApp() {
		super("Prim Maze", 600, 360, 2);
	}

	@Override
	public void run() {
		Integer startCell = grid.cell(GridPosition.TOP_LEFT);
		while (true) {
			new PrimMST<>(grid).accept(startCell);
			new BFSAnimation(canvas, grid).runAnimation(startCell);
			sleep(1000);
			clear();
		}
	}
}