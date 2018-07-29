package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.PlayScene;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;

public class PacManApp extends Application {

	/** Tile size. */
	public static final int TS = 16;

	public static void main(String[] args) {
		launch(new PacManApp(args));
	}

	public final Maze maze;

	public PacManApp(String[] args) {
		maze = new Maze(Assets.text("maze.txt"));
		settings.width = maze.numCols() * TS;
		settings.height = (maze.numRows() + 5) * TS;
		settings.scale = args.length > 0 ? Float.parseFloat(args[0]) : 1;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height,
				settings.scale);
		Game.fnPulse = pulse::getFrequency;
	}

	@Override
	public void init() {
		setController(new PlayScene(this));
	}
}