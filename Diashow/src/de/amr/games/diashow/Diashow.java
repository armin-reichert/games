package de.amr.games.diashow;

import de.amr.easy.game.Application;
import de.amr.games.diashow.screens.DiashowScreen;

public class Diashow extends Application {

	public static void main(String[] args) {
		Settings.title = "Diashow";
		Settings.width = 900;
		Settings.height = 600;
		Settings.fps = 5;
		launch(new Diashow());
	}

	@Override
	protected void init() {
		Assets.image("0.jpg");
		Assets.image("1.jpg");
		Assets.image("2.jpg");
		Assets.image("3.jpg");
		Views.add(new DiashowScreen(this));
		Views.show(DiashowScreen.class);
	}
}
