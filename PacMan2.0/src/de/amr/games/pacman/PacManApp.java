package de.amr.games.pacman;

import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.PlayScene;
import de.amr.games.pacman.ui.Spritesheet;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp(args));
	}

	public final Maze maze;

	public PacManApp(String[] args) {
		maze = new Maze(Assets.text("maze.txt"));
		settings.width = maze.numCols() * Spritesheet.TS;
		settings.height = (maze.numRows() + 5) * Spritesheet.TS;
		settings.scale = args.length > 0 ? Float.parseFloat(args[0]) : 1;
		settings.title = "Armin's PacMan";
		pulse.setFrequency(60);
		LOG.setLevel(Level.INFO);
	}

	@Override
	public void init() {
		setController(new PlayScene(this));
	}
}