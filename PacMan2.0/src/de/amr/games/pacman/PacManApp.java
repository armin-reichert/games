package de.amr.games.pacman;

import java.util.logging.Level;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.pacman.actor.GameActors;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.model.Maze;
import de.amr.games.pacman.ui.EnhancedGameUI;
import de.amr.games.pacman.ui.GameUI;
import de.amr.games.pacman.ui.Spritesheet;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp(args));
	}

	private final Game game;

	public PacManApp(String[] args) {
		Maze maze = new Maze(Assets.text("maze.txt"));
		game = new Game(maze, pulse::getFrequency);
		settings.width = maze.numCols() * Spritesheet.TS;
		settings.height = (maze.numRows() + 5) * Spritesheet.TS;
		settings.scale = args.length > 0 ? Float.parseFloat(args[0]) : 1;
		settings.title = "Armin's PacMan";
	}

	@Override
	public void init() {
		LOG.setLevel(Level.INFO);
		GameActors actors = new GameActors(game);
		GameUI gameUI = new GameUI(settings.width, settings.height, game, actors);
		GameController gameController = new GameController(game, actors, new EnhancedGameUI(gameUI));
		setController(gameController);
		pulse.setFrequency(60);
	}
}