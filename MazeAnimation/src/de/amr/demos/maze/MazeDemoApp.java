package de.amr.demos.maze;

import java.awt.Color;

import de.amr.demos.maze.bfs.BFSTraversalScene;
import de.amr.demos.maze.scene.generation.MazeGeneration;
import de.amr.demos.maze.scene.menu.Menu;
import de.amr.demos.maze.ui.GridAnimation;
import de.amr.easy.game.Application;
import de.amr.easy.game.scene.ActiveScene;
import de.amr.easy.game.ui.FullScreen;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.grid.impl.ObservableGrid;

public class MazeDemoApp extends Application {

	public static void main(String[] args) {
		launch(new MazeDemoApp());
	}

	public MazeDemoApp() {
		settings.title = "Maze Generation Demo";
		settings.bgColor = Color.WHITE;
		settings.fullScreenMode = FullScreen.Mode(1280, 800, 32);
		settings.width = 640;
		settings.height = 640;
		settings.set("cellSize", 4);
		pulse.setFrequency(30);
	}

	public ActiveScene<MazeDemoApp> menuScene, generationScene, traversalScene;
	private ObservableGrid<TraversalState, Integer> grid;
	private GridAnimation animation;

	@Override
	public void init() {
		menuScene = new Menu(this);
		generationScene = new MazeGeneration(this);
		traversalScene = new BFSTraversalScene(this);
		int cellSize = settings.getAsInt("cellSize");
		grid = new ObservableGrid<>(getWidth() / cellSize, getHeight() / cellSize, TraversalState.UNVISITED);
		animation = new GridAnimation(grid, cellSize, getWidth(), getHeight());
		select(menuScene);
	}

	public ObservableGrid<TraversalState, Integer> getGrid() {
		return grid;
	}

	public GridAnimation getAnimation() {
		return animation;
	}
}
