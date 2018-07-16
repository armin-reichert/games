package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Maze;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp());
	}

	/** Tile size of the board. */
	public static final int TS = 16;

	public PacManApp() {
		Maze maze = Maze.of(Assets.text("maze.txt"));
		settings.width = maze.numCols() * TS;
		settings.height = (maze.numRows() + 5) * TS;
		settings.scale = 1.25f;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height, settings.scale);
	}

	@Override
	public void init() {
		select(new GameController(this));
	}
}