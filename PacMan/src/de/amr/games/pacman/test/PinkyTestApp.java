package de.amr.games.pacman.test;

import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pacman.core.app.AbstractPacManApp;
import de.amr.games.pacman.theme.ModernTheme;

/**
 * Tests Pinky's behavior.
 * 
 * @author Armin Reichert
 */
public class PinkyTestApp extends AbstractPacManApp {

	public static void main(String... args) {
		PinkyTestApp app = new PinkyTestApp();
		app.settings.title = "Pinky behaviour test";
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
		setTheme(ModernTheme.class);
		views.add(new PinkyTestScene(this));
		views.show(PinkyTestScene.class);
	}
}