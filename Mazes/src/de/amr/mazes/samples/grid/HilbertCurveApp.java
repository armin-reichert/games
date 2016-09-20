package de.amr.mazes.samples.grid;

import static de.amr.easy.grid.api.Direction.E;
import static de.amr.easy.grid.api.Direction.N;
import static de.amr.easy.grid.api.Direction.S;
import static de.amr.easy.grid.api.Direction.W;
import static de.amr.easy.grid.api.GridPosition.BOTTOM_LEFT;
import static de.amr.easy.grid.api.GridPosition.BOTTOM_RIGHT;
import static de.amr.easy.grid.api.GridPosition.TOP_LEFT;
import static de.amr.easy.grid.api.GridPosition.TOP_RIGHT;

import java.util.stream.Stream;

import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.grid.iterators.traversals.HilbertCurve;
import de.amr.easy.maze.misc.Utils;
import de.amr.mazes.swing.rendering.BFSAnimation;

/**
 * Hilbert curve sample.
 * 
 * @author Armin Reichert
 */
public class HilbertCurveApp extends GridSampleApp {

	private static final int WIDTH = 512;
	private static final int HEIGHT = 512;
	private static final int MIN_CELLSIZE = 2;
	private static final int MAX_CELLSIZE = 256;

	public static void main(String[] args) {
		launch(new HilbertCurveApp());
	}

	private HilbertCurveApp() {
		super("Hilbert Curve", WIDTH / MAX_CELLSIZE, HEIGHT / MAX_CELLSIZE, MAX_CELLSIZE);
	}

	@Override
	protected String composeTitle() {
		return super.composeTitle() + ", " + grid.numEdges() + " edges";
	}

	private HilbertCurve createCurve(GridPosition startPos, int depth) {
		switch (startPos) {
		case TOP_RIGHT:
			return new HilbertCurve(depth, N, E, S, W);
		case TOP_LEFT:
			return new HilbertCurve(depth, N, W, S, E);
		case BOTTOM_RIGHT:
			return new HilbertCurve(depth, E, S, W, N);
		case BOTTOM_LEFT:
			return new HilbertCurve(depth, W, S, E, N);
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void run() {
		setDelay(5);
		Stream.of(TOP_RIGHT, TOP_LEFT, BOTTOM_LEFT, BOTTOM_RIGHT).forEach(startPos -> {
			int cellSize = MAX_CELLSIZE;
			while (cellSize >= MIN_CELLSIZE) {
				int depth = Utils.log(2, WIDTH / cellSize);
				HilbertCurve curve = createCurve(startPos, depth);
				CurveUtil.buildGrid(grid, curve, grid.cell(startPos),
						() -> window.setTitle(composeTitle()));
				BFSAnimation bfs = new BFSAnimation(canvas, grid);
				bfs.setDistancesVisible(false);
				bfs.runAnimation(grid.cell(startPos));
				cellSize /= 2;
				resize(WIDTH, HEIGHT, cellSize);
			}
		});
	}
}