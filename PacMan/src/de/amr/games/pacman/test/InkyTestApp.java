package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * The Inky behavior test app.
 * 
 * @author Armin Reichert
 */
public class InkyTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		InkyTestApp app = new InkyTestApp();
		app.settings.title = "Inky behaviour test";
		app.settings.width = NUM_COLS * TILE_SIZE;
		app.settings.height = NUM_ROWS * TILE_SIZE;
		app.settings.scale = args.length > 0 ? Float.valueOf(args[0]) / app.settings.height : 1;
		app.settings.fullScreenOnStart = false;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		app.settings.set("drawInternals", true);
		app.settings.set("drawGrid", true);
		app.settings.set("drawRoute", true);
		app.gameLoop.log = false;
		app.gameLoop.setTargetFrameRate(60);
		launch(app);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new InkyTestScene(this));
		views.show(InkyTestScene.class);
	}
}