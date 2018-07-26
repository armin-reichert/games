package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.PlayScene;
import de.amr.games.pacman.model.Maze;

public class PacManApp extends Application {

	/** Tile size. */
	public static final int TS = 16;

	public static void main(String[] args) {
		float scaling = 1f;
		if (args.length > 0) {
			scaling = Float.parseFloat(args[0]);
		}
		launch(new PacManApp(scaling));
	}

	private final Maze maze;

	public PacManApp(float scaling) {
		maze = new Maze(Assets.text("maze.txt"));
		settings.width = maze.numCols() * TS;
		settings.height = (maze.numRows() + 5) * TS;
		settings.scale = scaling;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height,
				settings.scale);
	}

	public Maze getMaze() {
		return maze;
	}

	@Override
	public void init() {
		select(new PlayScene(this));
	}
}