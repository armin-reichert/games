package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * Tests random movement inside the maze.
 * 
 * @author Armin Reichert
 */
public class RandomMoveTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		RandomMoveTestApp app = new RandomMoveTestApp();
		app.settings.title = "Random movement test";
		app.settings.width = NUM_COLS * TILE_SIZE;
		app.settings.height = NUM_ROWS * TILE_SIZE;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		launch(app);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new RandomMoveTestScene(this));
		views.show(RandomMoveTestScene.class);
	}
}