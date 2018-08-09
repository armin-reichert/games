package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.Spritesheet;

/**
 * Pac-Man game.
 * 
 * @author Armin Reichert
 *
 */
public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp(args));
	}

	private GameController gameControl;

	public PacManApp(String[] args) {
		Maze maze = new Maze(Assets.text("maze.txt"));
		settings.width = maze.numCols() * Spritesheet.TS;
		settings.height = (maze.numRows() + 5) * Spritesheet.TS;
		settings.scale = args.length > 0 ? Float.parseFloat(args[0]) : 1;
		settings.title = "Armin's PacMan";
		pulse.setFrequency(60);
		gameControl = new GameController(maze, settings.width, settings.height, pulse::getFrequency);
	}

	@Override
	public void init() {
		setController(gameControl);
	}
}