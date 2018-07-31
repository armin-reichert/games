package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.PlayScene;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.MazeUI;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp(args));
	}

	public final Maze maze;

	public PacManApp(String[] args) {
		maze = new Maze(Assets.text("maze.txt"));
		settings.width = maze.numCols() * MazeUI.TS;
		settings.height = (maze.numRows() + 5) * MazeUI.TS;
		settings.scale = args.length > 0 ? Float.parseFloat(args[0]) : 1;
		settings.title = "PacMan 2.0";
	}

	@Override
	public void init() {
		setController(new PlayScene(this));
	}
}