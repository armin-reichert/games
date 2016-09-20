package de.amr.mazes.samples.maze;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.maze.algorithms.HuntAndKill;
import de.amr.mazes.samples.grid.GridSampleApp;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class HuntAndKillApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new HuntAndKillApp());
	}

	public HuntAndKillApp() {
		super("Hunt And Kill", 300, 180, 4);
	}

	@Override
	public void run() {
		setDelay(0);
		while (true) {
			new HuntAndKill<>(grid).accept(grid.cell(GridPosition.CENTER));
			new BFSAnimation(canvas, grid).runAnimation(grid.cell(GridPosition.TOP_LEFT));
			sleep(1000);
			clear();
		}
	}
}