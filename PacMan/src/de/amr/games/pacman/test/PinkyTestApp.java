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
		PinkyTestApp app = new PinkyTestApp();
		app.settings.title = "Pinky behaviour test";
		app.settings.width = 448;
		app.settings.height = 576;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		app.settings.set("drawInternals", true);
		app.settings.set("drawGrid", true);
		app.settings.set("drawRoute", true);
		launch(app);
	}

	@Override
	protected void init() {
		views.add(new PinkyTestScene(this));
		views.show(PinkyTestScene.class);
	}
}