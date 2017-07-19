package de.amr.games.breakout;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.Score;
import de.amr.games.breakout.entities.Ball;
import de.amr.games.breakout.entities.Bat;
import de.amr.games.breakout.entities.ScoreDisplay;
import de.amr.games.breakout.scenes.PlayScene;

public class BreakoutGame extends Application {

	public enum SceneID {
		PlayScene
	}

	public static void main(String[] args) {
		launch(new BreakoutGame());
	}

	private final Score score;

	public BreakoutGame() {
		settings.title = "Breakout";
		settings.width = 300;
		settings.height = 300;
		settings.scale = 2;
		score = new Score();
	}

	@Override
	public void init() {

		assets.image("Background/background.jpg");
		assets.image("Balls/ball_green.png");
		assets.image("Bats/bat_blue.png");
		assets.sound("Sounds/plop.mp3");
		assets.sound("Sounds/point.mp3");

		entities.add(new Ball(assets, getWidth(), getHeight()));
		entities.add(new Bat(assets, getWidth()));
		entities.add(new ScoreDisplay(this));

		views.select(new PlayScene(this));
	}

	public Score getScore() {
		return score;
	}
}
