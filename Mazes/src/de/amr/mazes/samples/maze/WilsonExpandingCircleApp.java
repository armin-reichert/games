package de.amr.mazes.samples.maze;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.wilson.WilsonUSTExpandingCircle;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class WilsonExpandingCircleApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new WilsonExpandingCircleApp());
	}

	public WilsonExpandingCircleApp() {
		super("Wilson UST / Expanding Circle", 600, 360, 2);
	}

	@Override
	public void run() {
		Integer startCell = grid.cell(GridPosition.CENTER);
		setDelay(0);
		while (true) {
			new WilsonUSTExpandingCircle<>(grid).accept(startCell);
			new BFSAnimation(canvas, grid).runAnimation(startCell);
			sleep(1000);
			clear();
		}
	}
}