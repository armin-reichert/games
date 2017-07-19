package de.amr.games.pacman.test;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;

/**
 * Tests Pinky's behavior.
 * 
 * @author Armin Reichert
 */
public class PinkyTestApp extends Application {

	public static void main(String... args) {
		launch(new PinkyTestApp());
	}

	public PinkyTestApp() {
		settings.title = "Pinky behaviour test";
		settings.width = 448;
		settings.height = 576;
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.set("drawInternals", true);
		settings.set("drawGrid", true);
		settings.set("drawRoute", true);
	}

	@Override
	public void init() {
		views.select(new PinkyTestScene(this));
	}
}