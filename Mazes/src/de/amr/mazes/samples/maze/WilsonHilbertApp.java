package de.amr.mazes.samples.maze;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.wilson.WilsonUSTHilbertCurve;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class WilsonHilbertApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new WilsonHilbertApp());
	}

	public WilsonHilbertApp() {
		super("Wilson UST / Hilbert Curve Maze", 600, 360, 2);
		Integer startCell = grid.cell(GridPosition.TOP_LEFT);
		work = () -> {
			while (true) {
				new WilsonUSTHilbertCurve<>(grid).accept(startCell);
				new BFSAnimation(canvas, grid).runAnimation(startCell);
				sleep(3000);
				clear();
			}
		};
	}
}