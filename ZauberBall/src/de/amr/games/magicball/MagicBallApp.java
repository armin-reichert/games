package de.amr.games.magicball;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.magicball.views.DrawingScene;

public class MagicBallApp extends Application {

	public static void main(String[] args) {
		launch(new MagicBallApp());
	}

	public MagicBallApp() {
		settings.title = "Zauberball";
		settings.width = 800;
		settings.height = 600;
		settings.bgColor = Color.WHITE;
		settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		settings.fullScreenOnStart = false;
	}

	@Override
	public void init() {
		assets.image("ball.png");
		views.select(new DrawingScene(this));
	}
}