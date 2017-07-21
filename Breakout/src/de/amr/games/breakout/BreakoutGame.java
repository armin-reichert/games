package de.amr.games.breakout;

import de.amr.easy.game.Application;
import de.amr.games.breakout.scenes.PlayScene;

/**
 * A simple "Breakout" game.
 * 
 * @author Armin Reichert & Anna Schillo
 *
 */
public class BreakoutGame extends Application {

	public static void main(String[] args) {
		launch(new BreakoutGame());
	}

	public BreakoutGame() {
		settings.title = "Breakout";
		settings.width = 800;
		settings.height = 600;
		settings.set("ball_size", 12);
		settings.set("bat_width", 96);
		settings.set("bat_height", 12);
	}

	@Override
	public void init() {
		assets.image("background.jpg");
		assets.image("ball_green.png");
		assets.image("bat_blue.png");
		assets.sound("Sounds/plop.mp3");
		assets.sound("Sounds/point.mp3");
		selectView(new PlayScene(this));
	}
}