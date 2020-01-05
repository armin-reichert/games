package de.amr.games.magicball;

import java.awt.Color;
import java.awt.DisplayMode;

import de.amr.easy.game.GenericApplication;
import de.amr.easy.game.assets.Assets;
import de.amr.games.magicball.views.DrawingScene;

public class MagicBallApp extends GenericApplication {

	public static void main(String[] args) {
		launch(new MagicBallApp(), args);
	}

	public MagicBallApp() {
		settings().title = "Zauberball";
		settings().width = 800;
		settings().height = 600;
		settings().bgColor = Color.WHITE;
		settings().fullScreenMode = new DisplayMode(800, 600, 32, DisplayMode.REFRESH_RATE_UNKNOWN);
		settings().fullScreenOnStart = false;
	}

	@Override
	public void init() {
		Assets.image("ball.png");
		setController(new DrawingScene(settings().width, settings().height, 3));
	}
}