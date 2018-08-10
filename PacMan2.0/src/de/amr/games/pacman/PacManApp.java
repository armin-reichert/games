package de.amr.games.pacman;

import static de.amr.games.pacman.view.Spritesheet.TS;

import de.amr.easy.game.Application;
import de.amr.games.pacman.controller.GameController;

/**
 * Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManApp extends Application {

	public static void main(String[] args) {
		float scale = 1f;
		if (args.length > 0) {
			try {
				scale = Float.parseFloat(args[0]);
			} catch (NumberFormatException e) {
				Application.logger.info("Illegal scaling value: " + args[0]);
			}
		}
		launch(new PacManApp(scale));
	}

	public PacManApp(float scale) {
		settings.width = 28 * TS;
		settings.height = 36 * TS;
		settings.scale = scale;
		settings.title = "Armin's PacMan";
	}

	@Override
	public void init() {
		setController(new GameController(pulse::getFrequency));
	}
}