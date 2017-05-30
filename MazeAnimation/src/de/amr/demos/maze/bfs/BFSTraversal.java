package de.amr.demos.maze.bfs;

import static de.amr.demos.maze.MazeDemoApp.App;
import static de.amr.easy.game.Application.Log;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.demos.maze.MazeDemoApp;
import de.amr.demos.maze.scene.generation.MazeGeneration;
import de.amr.demos.maze.scene.menu.Menu;
import de.amr.demos.maze.ui.GridAnimation;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.graph.alg.traversal.BreadthFirstTraversal;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.graph.api.WeightedEdge;
import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.grid.impl.ObservableGrid;

public class BFSTraversal extends Scene<MazeDemoApp> {

	private ObservableGrid<TraversalState, Integer> grid;
	private BreadthFirstTraversal<Integer, WeightedEdge<Integer, Integer>> bfs;
	private GridAnimation animation;
	private Thread bfsRunner;
	private Integer startCell;
	private int maxDistance;
	private boolean aborted;

	public BFSTraversal(MazeDemoApp app) {
		super(app);
	}

	@Override
	public void init() {
		aborted = false;
		grid = getApp().getGrid();
		startCell = grid.cell(GridPosition.TOP_LEFT);
		animation = getApp().getAnimation();
		bfsRunner = new Thread(() -> {
			animation.setDelay(0);
			Log.info("Start first BFS to compute maximum distance:");
			bfs = new BreadthFirstTraversal<>(grid, startCell);
			bfs.findPath(startCell);
			Log.info("BFS finished.");
			maxDistance = bfs.getMaxDistance();
			Log.info("Max distance: " + maxDistance);
			animation.setRenderingModel(
					new BFSAnimationRenderingModel(grid, getApp().settings.getInt("cellSize"), bfs, maxDistance));
			animation.setDelay(0);
			Log.info("Start second, animated BFS:");
			bfs.addObserver(animation);
			bfs.findPath(startCell);
			bfs.removeObserver(animation);
			Log.info("BFS finished.");
		}, "BreadFirstTraversal");
		grid.clearContent();
		bfsRunner.start();
		Log.info("BFS animation screen initialized.");
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
			App.views.show(MazeGeneration.class);
		}
		if (aborted) {
			stopBreadthFirstTraversal();
			App.views.show(Menu.class);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		animation.render(g);
	}

	private void stopBreadthFirstTraversal() {
		Log.info("Stopping BFS");
		while (bfsRunner.isAlive()) {
			/* wait for BFS thread to finish */
		}
		Log.info("BFS finished");
	}
}
