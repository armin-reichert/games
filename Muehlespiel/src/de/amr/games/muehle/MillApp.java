package de.amr.games.muehle;

import de.amr.easy.game.Application;

/**
 * Mühlespiel.
 * 
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class MillApp extends Application {

	public static void main(String[] args) {
		launch(new MillApp());
	}

	public MillApp() {
		settings.title = "Mühlespiel";
		settings.width = 800;
		settings.height = 800;
		pulse.setFrequency(30);
	}

	@Override
	public void init() {
		selectView(new PlayScene(this));
	}
}