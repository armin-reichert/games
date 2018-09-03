package de.amr.games.breakout;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.games.breakout.controller.PlayScene;

/**
 * A simple "Breakout" game.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class BreakoutGameApp extends Application {

	public static void main(String[] args) {
		launch(new BreakoutGameApp());
	}

	public BreakoutGameApp() {
		settings.title = "Breakout";
		settings.width = 800;
		settings.height = 600;
		settings.set("ball_size", 12);
		settings.set("bat_width", 96);
		settings.set("bat_height", 12);
	}

	@Override
	public void init() {
		Assets.image("background.jpg");
		Assets.image("ball_green.png");
		Assets.image("bat_blue.png");
		Assets.sound("Sounds/plop.mp3");
		Assets.sound("Sounds/point.mp3");
		setController(new PlayScene(this));
	}
}