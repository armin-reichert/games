package de.amr.games.magicball;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.magicball.views.DrawingScene;

public class MagicBallApp extends Application {

	public static final MagicBallApp App = new MagicBallApp();

	public static void main(String[] args) {
		App.settings.title = "Zauberball";
		App.settings.width = 800;
		App.settings.height = 600;
		App.settings.bgColor = Color.WHITE;
		App.settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		App.settings.fullScreenOnStart = false;
		launch(App);
	}

	@Override
	public void init() {
		assets.image("ball.png");
		views.add(new DrawingScene(this));
		views.show(DrawingScene.class);
	}
}
