package de.amr.games.breakout;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.Score;
import de.amr.games.breakout.scenes.PlayScene;

public class BreakoutGame extends Application {

	public static int BALL_SIZE = 15;
	public static int BAT_WIDTH = 8 * BALL_SIZE;
	public static int BAT_HEIGHT = BALL_SIZE;

	public static void main(String[] args) {
		launch(new BreakoutGame());
	}

	public final Score score;

	public BreakoutGame() {
		score = new Score();
		settings.title = "Breakout";
		settings.width = 800;
		settings.height = 600;
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