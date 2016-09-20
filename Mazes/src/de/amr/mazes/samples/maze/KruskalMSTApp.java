package de.amr.mazes.samples.maze;

import static de.amr.easy.grid.api.GridPosition.TOP_LEFT;

import de.amr.easy.maze.algorithms.KruskalMST;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class KruskalMSTApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new KruskalMSTApp());
	}

	public KruskalMSTApp() {
		super("Kruskal Maze", 600, 360, 2);
	}

	@Override
	public void run() {
		while (true) {
			new KruskalMST<>(grid).accept(null);
			new BFSAnimation(canvas, grid).runAnimation(grid.cell(TOP_LEFT));
			sleep(1000);
			clear();
		}
	}
}