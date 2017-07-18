package de.amr.games.pacman.test;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;

/**
 * Test application for routing through the maze.
 * 
 * @author Armin Reichert
 */
public class RoutingTestApp extends Application {

	public static void main(String... args) {
		RoutingTestApp app = new RoutingTestApp();
		app.settings.title = "Pac-Man routing test app";
		app.settings.width = 448;
		app.settings.height = 576;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		app.settings.set("drawInternals", true);
		app.settings.set("drawGrid", true);
		app.settings.set("drawRoute", false);
		launch(app);
	}

	@Override
	public void init() {
		views.add(new RoutingTestScene(this));
		views.select(RoutingTestScene.class);
	}
}