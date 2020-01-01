package de.amr.games.diashow;

import de.amr.easy.game.GenericApplication;
import de.amr.easy.game.assets.Assets;
import de.amr.games.diashow.screens.DiashowScreen;

public class DiashowApp extends GenericApplication {

	public static void main(String[] args) {
		launch(new DiashowApp(), args);
	}

	public DiashowApp() {
		settings.title = "Diashow";
		settings.width = 900;
		settings.height = 600;
	}

	@Override
	public void init() {
		clock.setFrequency(3);
		Assets.image("0.jpg");
		Assets.image("1.jpg");
		Assets.image("2.jpg");
		Assets.image("3.jpg");
		DiashowScreen scene = new DiashowScreen(900, 600);
		setController(scene);
	}
}