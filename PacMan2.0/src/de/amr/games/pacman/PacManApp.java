package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.games.pacman.model.GameState;
import de.amr.games.pacman.ui.PlayScene;

public class PacManApp extends Application {

	public static void main(String[] args) {
		launch(new PacManApp());
	}

	/** Tile size of the board. */
	public static final int TS = 16;

	private final GameState gameState;

	public PacManApp() {
		gameState = new GameState();
		settings.width = gameState.mazeContent.numCols() * PacManApp.TS;
		settings.height = (gameState.mazeContent.numRows() + 5) * PacManApp.TS;
		settings.scale = 1.25f;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height, settings.scale);
	}

	public GameState getGameState() {
		return gameState;
	}

	@Override
	public void init() {
		select(new PlayScene(this));
	}
}