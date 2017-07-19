package de.amr.games.diashow;

import de.amr.easy.game.Application;
import de.amr.games.diashow.screens.DiashowScreen;

public class Diashow extends Application {

	public static void main(String[] args) {
		launch(new Diashow());
	}

	public Diashow() {
		settings.title = "Diashow";
		settings.width = 900;
		settings.height = 600;
		pulse.setFrequency(10);
	}

	@Override
	public void init() {
		assets.image("0.jpg");
		assets.image("1.jpg");
		assets.image("2.jpg");
		assets.image("3.jpg");
		addView(new DiashowScreen(this));
		selectView(DiashowScreen.class);
	}
}
