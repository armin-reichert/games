package de.amr.games.pacman.test;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * The Blinky behavior test app.
 * 
 * @author Armin Reichert
 */
public class BlinkyTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		BlinkyTestApp app = new BlinkyTestApp();
		app.settings.title = "Blinky behaviour test";
		app.settings.width = 448;
		app.settings.height = 576;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 16);
		app.settings.set("drawInternals", true);
		app.settings.set("drawGrid", true);
		app.settings.set("drawRoute", true);
		app.motor.setFrequency(60);
		launch(app);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new BlinkyTestScene(this));
		views.show(BlinkyTestScene.class);
	}
}