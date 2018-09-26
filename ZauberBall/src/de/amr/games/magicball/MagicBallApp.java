package de.amr.games.magicball;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.FullScreenMode;
import de.amr.games.magicball.views.DrawingScene;

public class MagicBallApp extends Application {

	public static void main(String[] args) {
		launch(new MagicBallApp(), args);
	}

	public MagicBallApp() {
		settings.title = "Zauberball";
		settings.width = 800;
		settings.height = 600;
		settings.bgColor = Color.WHITE;
		settings.fullScreenMode = new FullScreenMode(800, 600, 32);
		settings.fullScreenOnStart = false;
	}

	@Override
	public void init() {
		Assets.image("ball.png");
		setController(new DrawingScene(settings.width, settings.height, 3));
	}
}