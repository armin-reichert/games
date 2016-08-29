package de.amr.mazes.samples.maze;

import static de.amr.easy.grid.api.GridPosition.TOP_LEFT;

import de.amr.easy.maze.algorithms.IterativeDFS;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class IterativeDFSApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new IterativeDFSApp());
	}

	private static final int WIN_WIDTH = 1024;
	private static final int WIN_HEIGHT = 512;

	private static final int MIN_CELLSIZE = 2;
	private static final int MAX_CELLSIZE = 64;

	public IterativeDFSApp() {
		super("DFS Maze Generation", WIN_WIDTH / MAX_CELLSIZE, WIN_HEIGHT / MAX_CELLSIZE, MAX_CELLSIZE);
		work = () -> {
			int cellSize = MAX_CELLSIZE;
			do {
				new IterativeDFS<>(grid).accept(grid.cell(TOP_LEFT));
				new BFSAnimation(canvas, grid).runAnimation(grid.cell(TOP_LEFT));
				sleep(2000);
				cellSize /= 2;
				if (cellSize < MIN_CELLSIZE) {
					cellSize = MAX_CELLSIZE;
				}
				resize(WIN_WIDTH, WIN_HEIGHT, cellSize);
			} while (true);
		};
	}
}