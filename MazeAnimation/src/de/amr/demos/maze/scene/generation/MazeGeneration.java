package de.amr.demos.maze.scene.generation;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Logger;

import de.amr.demos.maze.MazeDemoApp;
import de.amr.demos.maze.bfs.BFSTraversalScene;
import de.amr.demos.maze.scene.menu.Menu;
import de.amr.demos.maze.ui.GridAnimation;
import de.amr.demos.maze.ui.GridVisualization;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.grid.api.Grid2D;
import de.amr.easy.grid.api.GridPosition;
import de.amr.easy.grid.impl.ObservableGrid;
import de.amr.easy.maze.alg.BinaryTree;
import de.amr.easy.maze.alg.BinaryTreeRandom;
import de.amr.easy.maze.alg.Eller;
import de.amr.easy.maze.alg.EllerInsideOut;
import de.amr.easy.maze.alg.HuntAndKill;
import de.amr.easy.maze.alg.IterativeDFS;
import de.amr.easy.maze.alg.KruskalMST;
import de.amr.easy.maze.alg.MazeAlgorithm;
import de.amr.easy.maze.alg.PrimMST;
import de.amr.easy.maze.alg.RandomBFS;
import de.amr.easy.maze.alg.RecursiveDivision;
import de.amr.easy.maze.alg.wilson.WilsonUSTHilbertCurve;
import de.amr.easy.maze.alg.wilson.WilsonUSTNestedRectangles;

public class MazeGeneration extends Scene<MazeDemoApp> {

	private static final Logger LOG = Logger.getLogger(MazeGeneration.class.getName());

	private static final Class<?>[] ALGORITHMS = {
		/*@formatter:off*/
		BinaryTree.class, BinaryTreeRandom.class, Eller.class,
		EllerInsideOut.class, HuntAndKill.class, IterativeDFS.class, KruskalMST.class, PrimMST.class, RandomBFS.class,
		RecursiveDivision.class, WilsonUSTHilbertCurve.class, WilsonUSTNestedRectangles.class
		/*@formatter:on*/
	};

	private ObservableGrid<TraversalState, Integer> grid;
	private MazeAlgorithm algorithm;
	private Integer startCell;
	private Thread mazeGeneration;
	private GridAnimation animation;
	private boolean aborted;

	public MazeGeneration(MazeDemoApp game) {
		super(game);
	}

	@Override
	public void init() {
		aborted = false;
		grid = app.getGrid();
		animation = app.getAnimation();
		startCell = grid.cell(GridPosition.TOP_LEFT);
		mazeGeneration = new Thread(() -> {
			chooseRandomAlgorithm();
			animation.setRenderingModel(new GridVisualization(grid, app.settings.getAsInt("cellSize")));
			animation.clearCanvas();
			prepareGrid(algorithm);
			algorithm.accept(startCell);
		}, "MazeGeneration");
		mazeGeneration.start();
		LOG.info("Maze generation screen initialized.");
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_CONTROL) && Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			aborted = true;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER) && !mazeGeneration.isAlive()) {
			app.views.select(MazeGeneration.class);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_PLUS)) {
			animation.faster(1);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_MINUS)) {
			animation.slower(1);
		}
		if (aborted) {
			stopGeneration();
			app.views.select(Menu.class);
		} else if (!mazeGeneration.isAlive()) {
			app.views.select(BFSTraversalScene.class);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		animation.render(g);
	}

	private void chooseRandomAlgorithm() {
		Class<?> algorithmClass = ALGORITHMS[new Random().nextInt(ALGORITHMS.length)];
		try {
			algorithm = (MazeAlgorithm) algorithmClass.getConstructor(Grid2D.class).newInstance(grid);
			LOG.info("Maze generation algorithm: " + algorithmClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void prepareGrid(Consumer<Integer> algorithm) {
		grid.clearContent();
		grid.removeEdges();
	}

	private void stopGeneration() {
		LOG.info("Stopping maze generation, this may take some time...");
		while (mazeGeneration.isAlive()) {
			/* wait for generator to finish */
		}
		LOG.info("Maze generation finished");
	}
}