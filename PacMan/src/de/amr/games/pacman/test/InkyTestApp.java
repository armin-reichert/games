package de.amr.games.pacman.test;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;

/**
 * Tests Inky's behavior.
 * 
 * @author Armin Reichert
 */
public class InkyTestApp extends Application {

	public static void main(String... args) {
		launch(new InkyTestApp());
	}

	public InkyTestApp() {
		settings.title = "Inky behaviour test";
		settings.width = 448;
		settings.height = 576;
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.set("drawInternals", true);
		settings.set("drawGrid", true);
		settings.set("drawRoute", true);
	}

	@Override
	public void init() {
		views.add(new InkyTestScene(this));
		views.select(InkyTestScene.class);
	}
}