package de.amr.demos.maze;

import java.awt.Color;

import de.amr.demos.maze.bfs.BFSTraversal;
import de.amr.demos.maze.scene.generation.MazeGeneration;
import de.amr.demos.maze.scene.menu.Menu;
import de.amr.demos.maze.ui.GridAnimation;
import de.amr.easy.game.Application;
import de.amr.easy.graph.api.TraversalState;
import de.amr.easy.grid.impl.ObservableGrid;

public class MazeDemoApp extends Application {

	public static final MazeDemoApp App = new MazeDemoApp();

	public static void main(String[] args) {
		App.settings.title = "Maze Generation Demo";
		App.settings.bgColor = Color.WHITE;
		// App.Settings.fullscreen = FullScreen.Mode(1280, 800, 32);
		App.settings.width = 640;
		App.settings.height = 640;
		App.settings.fps = 30;
		App.settings.set("cellSize", 4);
		launch(App);
	}

	private ObservableGrid<TraversalState, Integer> grid;
	private GridAnimation animation;

	@Override
	protected void init() {
		views.add(new Menu(this));
		views.add(new MazeGeneration(this));
		views.add(new BFSTraversal(this));
		int cellSize = settings.getInt("cellSize");
		grid = new ObservableGrid<>(getWidth() / cellSize, getHeight() / cellSize, TraversalState.UNVISITED);
		animation = new GridAnimation(grid, cellSize, getWidth(), getHeight());
		views.show(Menu.class);
	}

	public ObservableGrid<TraversalState, Integer> getGrid() {
		return grid;
	}

	public GridAnimation getAnimation() {
		return animation;
	}
}
