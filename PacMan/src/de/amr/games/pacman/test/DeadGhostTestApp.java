package de.amr.games.pacman.test;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;

/**
 * The Blinky behavior test app.
 * 
 * @author Armin Reichert
 */
public class DeadGhostTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		DeadGhostTestApp app = new DeadGhostTestApp();
		app.settings.title = "Dead ghost test";
		app.settings.width = 448;
		app.settings.height = 576;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		app.settings.set("drawInternals", true);
		app.settings.set("drawGrid", true);
		app.settings.set("drawRoute", true);
		app.motor.setFrequency(60);
		launch(app);
	}

	@Override
	protected void init() {
		super.init();
		views.add(new DeadGhostTestScene(this));
		views.show(DeadGhostTestScene.class);
	}
}