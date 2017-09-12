package de.amr.demos.maze.bfs;

import static de.amr.easy.game.Application.LOG;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.demos.maze.MazeDemoApp;
import de.amr.demos.maze.ui.GridAnimation;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.easy.graph.alg.traversal.BreadthFirstTraversal;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.api.WeightedEdge;
import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.grid.impl.ObservableGrid;

public class BFSTraversalScene extends ActiveScene<MazeDemoApp> {

	private ObservableGrid<TraversalState, Integer> grid;
	private BreadthFirstTraversal<Integer, WeightedEdge<Integer, Integer>> bfs;
	private GridAnimation animation;
	private Thread bfsRunner;
	private Integer startCell;
	private int maxDistance;
	private boolean aborted;

	public BFSTraversalScene(MazeDemoApp app) {
		super(app);
	}

	@Override
	public void init() {
		aborted = false;
		grid = app.getGrid();
		startCell = grid.cell(GridPosition.TOP_LEFT);
		animation = app.getAnimation();
		bfsRunner = new Thread(() -> {
			animation.setDelay(0);
			LOG.info("Start first BFS to compute maximum distance:");
			bfs = new BreadthFirstTraversal<>(grid, startCell);
			bfs.findPath(startCell);
			LOG.info("BFS finished.");
			maxDistance = bfs.getMaxDistance();
			LOG.info("Max distance: " + maxDistance);
			animation
					.setRenderingModel(new BFSAnimationRenderingModel(grid, app.settings.getAsInt("cellSize"), bfs, maxDistance));
			animation.setDelay(0);
			LOG.info("Start second, animated BFS:");
			bfs.addObserver(animation);
			bfs.findPath(startCell);
			bfs.removeObserver(animation);
			LOG.info("BFS finished.");
		}, "BreadFirstTraversal");
		grid.clearContent();
		bfsRunner.start();
		LOG.info("BFS animation screen initialized.");
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_PLUS)) {
			animation.faster(1);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_MINUS)) {
			animation.slower(1);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_CONTROL) && Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			aborted = true;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER) && !bfsRunner.isAlive()) {
			app.select(app.generationScene);
		}
		if (aborted) {
			stopBreadthFirstTraversal();
			app.select(app.menuScene);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		animation.render(g);
	}

	private void stopBreadthFirstTraversal() {
		LOG.info("Stopping BFS");
		while (bfsRunner.isAlive()) {
			/* wait for BFS thread to finish */
		}
		LOG.info("BFS finished");
	}
}
