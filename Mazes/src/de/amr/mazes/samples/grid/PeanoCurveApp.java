package de.amr.mazes.samples.grid;

import static de.amr.easy.grid.api.GridPosition.BOTTOM_LEFT;

import de.amr.easy.grid.iterators.traversals.PeanoCurve;
import de.amr.easy.maze.misc.Utils;
import de.amr.mazes.swing.rendering.BFSAnimation;

public class PeanoCurveApp extends GridSampleApp {

	public static void main(String[] args) {
		launch(new PeanoCurveApp());
	}

	public PeanoCurveApp() {
		super("Peano Curve", 81, 81, 8);
	}

	@Override
	public void run() {
		setDelay(3);
		int depth = Utils.log(3, grid.numCols());
		while (true) {
			CurveUtil.followCurve(grid, new PeanoCurve(depth), grid.cell(BOTTOM_LEFT),
					() -> window.setTitle(composeTitle()));
			new BFSAnimation(canvas, grid).runAnimation(grid.cell(BOTTOM_LEFT));
			sleep(3000);
			clear();
		}
	}
}
