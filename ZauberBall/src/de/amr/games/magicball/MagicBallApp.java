package de.amr.games.magicball;

import java.awt.DisplayMode;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.config.AppSettings;
import de.amr.games.magicball.views.DrawingScene;

public class MagicBallApp extends Application {

	public static void main(String[] args) {
		launch(MagicBallApp.class, args);
	}

	@Override
	protected void configure(AppSettings settings) {
		settings.title = "Zauberball";
		settings.width = 800;
		settings.height = 600;
		settings.fullScreenMode = new DisplayMode(800, 600, 32, DisplayMode.REFRESH_RATE_UNKNOWN);
		settings.fullScreen = false;
	}

	@Override
	public void init() {
		Assets.image("ball.png");
		setController(new DrawingScene(settings().width, settings().height, 3));
	}
}