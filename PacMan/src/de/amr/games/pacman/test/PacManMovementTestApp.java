package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * The PacMan movement test app.
 * 
 * @author Armin Reichert
 */
public class PacManMovementTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		PacManMovementTestApp app = new PacManMovementTestApp();
		app.settings.title = "Pac-Man movement test";
		app.settings.width = NUM_COLS * TILE_SIZE;
		app.settings.height = NUM_ROWS * TILE_SIZE;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		app.settings.set("drawInternals", true);
		app.settings.set("drawGrid", true);
		app.settings.set("drawRoute", true);
		launch(app);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new PacManMovementTestScene(this));
		views.show(PacManMovementTestScene.class);
	}
}