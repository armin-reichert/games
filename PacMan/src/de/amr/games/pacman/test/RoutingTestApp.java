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
		launch(new RoutingTestApp());
	}

	public RoutingTestApp() {
		settings.title = "Pac-Man routing test app";
		settings.width = 448;
		settings.height = 576;
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.set("drawInternals", true);
		settings.set("drawGrid", true);
		settings.set("drawRoute", false);
	}

	@Override
	public void init() {
		selectView(new RoutingTestScene(this));
	}
}