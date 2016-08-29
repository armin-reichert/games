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
		Settings.title = "Breakout";
		Settings.width = 300;
		Settings.height = 300;
		Settings.scale = 2;
		launch(new BreakoutGame());
	}

	private final Score score;

	public BreakoutGame() {
		score = new Score();
	}

	@Override
	protected void init() {

		Assets.image("Background/background.jpg");
		Assets.image("Balls/ball_green.png");
		Assets.image("Bats/bat_blue.png");
		Assets.sound("Sounds/plop.mp3");
		Assets.sound("Sounds/point.mp3");

		Entities.add(new Ball(getWidth(), getHeight()));
		Entities.add(new Bat(getWidth()));
		Entities.add(new ScoreDisplay(this));

		Views.add(new PlayScene(this));
		Views.show(PlayScene.class);
	}

	public Score getScore() {
		return score;
	}
}
