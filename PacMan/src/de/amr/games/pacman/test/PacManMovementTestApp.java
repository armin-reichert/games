package de.amr.games.pacman.test;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;

/**
 * The PacMan movement test app.
 * 
 * @author Armin Reichert
 */
public class PacManMovementTestApp extends Application {

	public static void main(String... args) {
		launch(new PacManMovementTestApp());
	}

	public PacManMovementTestApp() {
		settings.title = "Pac-Man movement test";
		settings.width = 448;
		settings.height = 576;
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.set("drawInternals", true);
		settings.set("drawGrid", true);
		settings.set("drawRoute", true);
	}

	@Override
	public void init() {
		selectView(new PacManMovementTestScene(this));
	}
}