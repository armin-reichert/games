package de.amr.mazes.samples.maze;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.AldousBroderUST;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class AldousBroderUSTApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new AldousBroderUSTApp());
	}

	public AldousBroderUSTApp() {
		super("Aldous-Broder UST Maze", 100, 80, 8);
	}

	@Override
	public void run() {
		setDelay(0);
		Integer startCell = grid.cell(GridPosition.TOP_LEFT);
		while (true) {
			new AldousBroderUST<>(grid).accept(startCell);
			new BFSAnimation(canvas, grid).runAnimation(startCell);
			sleep(1000);
			clear();
		}
	};
}