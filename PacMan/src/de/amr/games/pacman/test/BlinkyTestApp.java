package de.amr.games.pacman.test;

import static de.amr.games.pacman.core.board.Board.NUM_COLS;
import static de.amr.games.pacman.core.board.Board.NUM_ROWS;
import static de.amr.games.pacman.theme.PacManTheme.TILE_SIZE;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * The Blinky behavior test app.
 * 
 * @author Armin Reichert
 */
public class BlinkyTestApp extends AbstractPacManApp {

	public static final BlinkyTestApp App = new BlinkyTestApp();

	public static void main(String... args) {
		App.settings.title = "Blinky behaviour test";
		App.settings.width = NUM_COLS * TILE_SIZE;
		App.settings.height = NUM_ROWS * TILE_SIZE;
		App.settings.scale = args.length > 0 ? Float.valueOf(args[0]) / App.settings.height : 1;
		App.settings.fullScreenOnStart = false;
		App.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		App.settings.set("drawInternals", true);
		App.settings.set("drawGrid", true);
		App.settings.set("drawRoute", true);
		App.gameLoop.log = false;
		App.gameLoop.setTargetFrameRate(60);
		launch(App);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new BlinkyTestScene(this));
		views.show(BlinkyTestScene.class);
	}
}