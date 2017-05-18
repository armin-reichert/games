package de.amr.games.pacman.test;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * Tests scattering behavior of ghosts.
 * 
 * @author Armin Reichert
 */
public class ScatteringTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		ScatteringTestApp app = new ScatteringTestApp();
		app.settings.title = "Ghost scattering test app";
		app.settings.width = 448;
		app.settings.height = 576;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		app.settings.set("drawInternals", true);
		launch(app);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new ScatteringTestScene(this));
		views.show(ScatteringTestScene.class);
	}
}