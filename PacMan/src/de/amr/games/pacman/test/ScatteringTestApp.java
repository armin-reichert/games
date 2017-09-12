package de.amr.games.pacman.test;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;

/**
 * Tests scattering behavior of ghosts.
 * 
 * @author Armin Reichert
 */
public class ScatteringTestApp extends Application {

	public static void main(String... args) {
		launch(new ScatteringTestApp());
	}

	public ScatteringTestApp() {
		settings.title = "Ghost scattering test app";
		settings.width = 448;
		settings.height = 576;
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.set("drawInternals", true);
	}

	@Override
	public void init() {
		select(new ScatteringTestScene(this));
	}
}