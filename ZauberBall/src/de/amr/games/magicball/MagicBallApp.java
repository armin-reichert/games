package de.amr.games.magicball;

import java.awt.Color;

import de.amr.easy.game.Application;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.magicball.views.DrawingScene;

public class MagicBallApp extends Application {

	public static void main(String[] args) {
		Settings.title = "Zauberball";
		Settings.width = 800;
		Settings.height = 600;
		Settings.bgColor = Color.WHITE;
		Settings.fullScreenMode = FullScreen.Mode(800, 600, 32);
		Settings.fullScreenOnStart = false;
		launch(new MagicBallApp());
	}

	@Override
	public void init() {
		Assets.image("ball.png");
		Views.add(new DrawingScene(this));
		Views.show(DrawingScene.class);
	}
}
