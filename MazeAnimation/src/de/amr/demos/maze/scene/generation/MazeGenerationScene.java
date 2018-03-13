package de.amr.demos.maze.scene.generation;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.logging.Logger;

import de.amr.demos.maze.MazeDemoApp;
import de.amr.demos.maze.ui.GridVisualization;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.easy.grid.api.Grid2D;
import de.amr.easy.grid.api.GridPosition;
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

public class MazeGenerationScene extends ActiveScene<MazeDemoApp> {

	private static final Logger LOG = Logger.getLogger(MazeGenerationScene.class.getName());

	private static final Class<?>[] ALGORITHMS = {
		/*@formatter:off*/
		BinaryTree.class, BinaryTreeRandom.class, Eller.class,
		EllerInsideOut.class, HuntAndKill.class, IterativeDFS.class, KruskalMST.class, PrimMST.class, RandomBFS.class,
		RecursiveDivision.class, WilsonUSTHilbertCurve.class, WilsonUSTNestedRectangles.class
		/*@formatter:on*/
	};

	private MazeAlgorithm algorithm;
	private Thread thread;
	private boolean stopped;

	public MazeGenerationScene(MazeDemoApp game) {
		super(game);
	}

	@Override
	public void init() {
		stopped = false;
		thread = new Thread(() -> {
			chooseRandomAlgorithm();
			app.getAnimation().setRenderingModel(
					new GridVisualization(app.getGrid(), app.settings.getAsInt("cellSize")));
			app.getAnimation().clearCanvas();
			app.getGrid().clearContent();
			app.getGrid().removeEdges();
			Integer startCell = app.getGrid().cell(GridPosition.TOP_LEFT);
			algorithm.accept(startCell);
		}, "MazeGeneration");
		thread.start();
		LOG.info("Maze generation screen initialized.");
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_CONTROL) && Keyboard.keyPressedOnce(KeyEvent.VK_C)) {
			stopped = true;
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_ENTER) && !thread.isAlive()) {
			app.select(app.generationScene);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_PLUS)) {
			app.getAnimation().faster(1);
		} else if (Keyboard.keyPressedOnce(KeyEvent.VK_MINUS)) {
			app.getAnimation().slower(1);
		}
		if (stopped) {
			stopGeneration();
			app.select(app.menuScene);
		} else if (!thread.isAlive()) {
			app.select(app.traversalScene);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		app.getAnimation().render(g);
	}

	private void chooseRandomAlgorithm() {
		Class<?> algorithmClass = ALGORITHMS[new Random().nextInt(ALGORITHMS.length)];
		try {
			algorithm = (MazeAlgorithm) algorithmClass.getConstructor(Grid2D.class)
					.newInstance(app.getGrid());
			LOG.info("Randomly chosen maze generation algorithm: " + algorithmClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void stopGeneration() {
		LOG.info("Stopping maze generation, this may take some time...");
		while (thread.isAlive()) {
			/* wait for generator to finish */
		}
		LOG.info("Maze generation finished");
	}
}