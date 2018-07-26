package de.amr.games.pacman;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.PlayScene;

public class PacManApp extends Application {

	/** Tile size. */
	public static final int TS = 16;

	public static void main(String[] args) {
		float scaling = 1f;
		if (args.length > 0) {
			scaling = Float.parseFloat(args[0]);
		}
		launch(new PacManApp(scaling));
	}

	public PacManApp(float scaling) {
		settings.width = 28 * TS;
		settings.height = 36 * TS;
		settings.scale = scaling;
		settings.title = String.format("PacMan 2.0 (%d x %d * %.2f)", settings.width, settings.height,
				settings.scale);
	}

	@Override
	public void init() {
		select(new PlayScene(this));
	}
}