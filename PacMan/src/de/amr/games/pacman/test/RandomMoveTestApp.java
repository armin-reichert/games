package de.amr.games.pacman.test;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;

/**
 * Tests random movement inside the maze.
 * 
 * @author Armin Reichert
 */
public class RandomMoveTestApp extends Application {

	public static void main(String... args) {
		RandomMoveTestApp app = new RandomMoveTestApp();
		app.settings.title = "Random movement test";
		app.settings.width = 448;
		app.settings.height = 576;
		app.settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		app.settings.set("drawInternals", true);
		launch(app);
	}

	@Override
	protected void init() {
		views.add(new RandomMoveTestScene(this));
		views.show(RandomMoveTestScene.class);
	}
}