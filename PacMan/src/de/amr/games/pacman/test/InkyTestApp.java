package de.amr.games.pacman.test;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * Tests Inky's behavior.
 * 
 * @author Armin Reichert
 */
public class InkyTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		InkyTestApp app = new InkyTestApp();
		app.settings.title = "Inky behaviour test";
		app.settings.width = 448;
		app.settings.height = 576;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		app.settings.set("drawInternals", true);
		app.settings.set("drawGrid", true);
		app.settings.set("drawRoute", true);
		launch(app);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new InkyTestScene(this));
		views.show(InkyTestScene.class);
	}
}