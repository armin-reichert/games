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
		PacManMovementTestApp app = new PacManMovementTestApp();
		app.settings.title = "Pac-Man movement test";
		app.settings.width = 448;
		app.settings.height = 576;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		app.settings.set("drawInternals", true);
		app.settings.set("drawGrid", true);
		app.settings.set("drawRoute", true);
		launch(app);
	}

	@Override
	public void init() {
		views.add(new PacManMovementTestScene(this));
		views.show(PacManMovementTestScene.class);
	}
}