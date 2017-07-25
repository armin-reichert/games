package de.amr.games.muehle;

import de.amr.easy.game.Application;
import de.amr.easy.game.scene.Scene;

/**
 * Mühlespiel.
 * 
 * @author Armin Reichert & Peter und Anna Schillo
 */
public class MuehleApp extends Application {

	public static void main(String[] args) {
		launch(new MuehleApp());
	}

	public MuehleApp() {
		settings.title = "Mühlespiel";
		settings.width = 800;
		settings.height = 800;
	}

	@Override
	public void init() {
		Scene<MuehleApp> scene = new SpielScene(this);
		addView(scene);
		selectView(scene);
	}
}