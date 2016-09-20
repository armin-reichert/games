package de.amr.mazes.samples.maze;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.EllerInsideOut;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class ArminsApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new ArminsApp());
	}

	public ArminsApp() {
		super("Armin's algorithm", 300, 180, 4);
	}

	@Override
	public void run() {
		setDelay(0);
		while (true) {
			new EllerInsideOut<>(grid).accept(null);
			Integer startCell = grid.cell(GridPosition.CENTER);
			new BFSAnimation(canvas, grid).runAnimation(startCell);
			sleep(1000);
			clear();
		}
	}
}
