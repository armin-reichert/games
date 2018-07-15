package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.ui.PlayScene;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp());
	}

	/** Tile size of the board. */
	public static final int TS = 16;

	private final Game gameState;

	public PacManApp() {
		gameState = new Game();
		settings.width = gameState.maze.numCols() * PacManApp.TS;
		settings.height = (gameState.maze.numRows() + 5) * PacManApp.TS;
		settings.scale = 1.25f;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height, settings.scale);
	}

	public Game getGameState() {
		return gameState;
	}

	@Override
	public void init() {
		select(new PlayScene(this));
	}
}