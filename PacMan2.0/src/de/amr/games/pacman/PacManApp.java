package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.PlayScene;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp());
	}

	/** Tile size of the board. */
	public static final int TS = 16;

	public PacManApp() {
		Maze maze = new Maze(Assets.text("maze.txt"));
		settings.width = maze.numCols() * PacManApp.TS;
		settings.height = (maze.numRows() + 5) * PacManApp.TS;
		settings.scale = 1.5f;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height, settings.scale);
	}

	@Override
	public void init() {
		select(new PlayScene(this));
	}
}