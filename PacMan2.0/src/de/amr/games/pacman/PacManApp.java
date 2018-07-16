package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Maze;

public class PacManApp extends Application {

	/** Tile size of the board. */
	public static int TS = 16;

	public static void main(String[] args) {
		if (args.length > 0) {
			TS = Integer.parseInt(args[0]);
		}
		launch(new PacManApp());
	}

	public PacManApp() {
		Maze maze = Maze.of(Assets.text("maze.txt"));
		settings.width = maze.numCols() * TS;
		settings.height = (maze.numRows() + 5) * TS;
		settings.scale = 1f;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height, settings.scale);
	}

	@Override
	public void init() {
		select(new GameController(this));
	}
}