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

	public static final BreakoutGame Game = new BreakoutGame();

	public static void main(String[] args) {
		Game.settings.title = "Breakout";
		Game.settings.width = 300;
		Game.settings.height = 300;
		Game.settings.scale = 2;
		launch(Game);
	}

	private final Score score;

	public BreakoutGame() {
		score = new Score();
	}

	@Override
	public void init() {

		assets.image("Background/background.jpg");
		assets.image("Balls/ball_green.png");
		assets.image("Bats/bat_blue.png");
		assets.sound("Sounds/plop.mp3");
		assets.sound("Sounds/point.mp3");

		entities.add(new Ball(getWidth(), getHeight()));
		entities.add(new Bat(getWidth()));
		entities.add(new ScoreDisplay(this));

		views.add(new PlayScene(this));
		views.show(PlayScene.class);
	}

	public Score getScore() {
		return score;
	}
}
