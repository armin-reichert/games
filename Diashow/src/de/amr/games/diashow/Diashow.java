package de.amr.games.diashow;

import de.amr.easy.game.Application;
import de.amr.games.diashow.screens.DiashowScreen;

public class Diashow extends Application {

	public static final Diashow App = new Diashow();

	public static void main(String[] args) {
		App.settings.title = "Diashow";
		App.settings.width = 900;
		App.settings.height = 600;
		App.motor.setFrequency(3);
		launch(App);
	}

	@Override
	protected void init() {
		assets.image("0.jpg");
		assets.image("1.jpg");
		assets.image("2.jpg");
		assets.image("3.jpg");
		views.add(new DiashowScreen(this));
		views.show(DiashowScreen.class);
	}
}
