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
		launch(new RandomMoveTestApp());
	}

	public RandomMoveTestApp() {
		settings.title = "Random movement test";
		settings.width = 448;
		settings.height = 576;
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.set("drawInternals", true);
	}

	@Override
	public void init() {
		views.select(new RandomMoveTestScene(this));
	}
}