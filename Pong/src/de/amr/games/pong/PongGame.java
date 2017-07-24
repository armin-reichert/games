package de.amr.games.pong;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_Y;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.entities.ScoreDisplay;
import de.amr.games.pong.scenes.menu.MenuScene;
import de.amr.games.pong.scenes.play.PongPlayScene;

/**
 * The classic "Pong" game with different play modes.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class PongGame extends Application {

	public enum PlayMode {
		Player1_Player2, Player1_Computer, Computer_Player2, Computer_Computer
	}

	public static void main(String[] args) {
		launch(new PongGame());
	}

	private Score scorePlayerLeft, scorePlayerRight;

	public PongGame() {
		settings.title = "Pong";
		settings.width = 640;
		settings.height = 480;
		settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
		scorePlayerLeft = new Score(points -> points == 11);
		scorePlayerRight = new Score(points -> points == 11);
	}

	@Override
	public void init() {
		Assets.sound("plop.mp3");
		Assets.sound("plip.mp3");
		Assets.sound("out.mp3");

		entities.add(new Court(getWidth(), getHeight()));
		entities.add(new ScoreDisplay(getScorePlayerLeft(), getScorePlayerRight()));

		entities.add(new Ball(getHeight()));

		entities.add(new AutoPaddleLeft(this));
		entities.add(new AutoPaddleRight(this));

		Paddle paddleLeft = new Paddle(this, VK_A, VK_Y);
		paddleLeft.setName("paddleLeft");
		entities.add(paddleLeft);

		Paddle paddleRight = new Paddle(this, VK_UP, VK_DOWN);
		paddleRight.setName("paddleRight");
		entities.add(paddleRight);

		addView(new MenuScene(this));
		addView(new PongPlayScene(this));
		selectView(MenuScene.class);

		setPlayMode(PlayMode.Player1_Player2);
	}

	public void setPlayMode(PlayMode playMode) {
		findView(MenuScene.class).setSelectedPlayMode(playMode);
	}

	public PlayMode getPlayMode() {
		return findView(MenuScene.class).getSelectedPlayMode();
	}

	public Score getScorePlayerLeft() {
		return scorePlayerLeft;
	}

	public Score getScorePlayerRight() {
		return scorePlayerRight;
	}
}