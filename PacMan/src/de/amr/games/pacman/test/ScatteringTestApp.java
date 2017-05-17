package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * Tests scattering behavior of ghosts.
 * 
 * @author Armin Reichert
 */
public class ScatteringTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		ScatteringTestApp app = new ScatteringTestApp();
		app.settings.title = "Ghost scattering test app";
		app.settings.width = NUM_COLS * TILE_SIZE;
		app.settings.height = NUM_ROWS * TILE_SIZE;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		app.settings.set("drawInternals", true);
		launch(app);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new ScatteringTestScene(this));
		views.show(ScatteringTestScene.class);
	}
}