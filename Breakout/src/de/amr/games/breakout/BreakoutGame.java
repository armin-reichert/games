package de.amr.games.breakout;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
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
		Assets.OBJECT.image("background.jpg");
		Assets.OBJECT.image("ball_green.png");
		Assets.OBJECT.image("bat_blue.png");
		Assets.OBJECT.sound("Sounds/plop.mp3");
		Assets.OBJECT.sound("Sounds/point.mp3");
		selectView(new PlayScene(this));
	}
}