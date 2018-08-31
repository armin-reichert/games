package de.amr.games.pong;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_Y;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.entities.Score;
import de.amr.games.pong.entities.ScoreDisplay;
import de.amr.games.pong.view.menu.MenuView;
import de.amr.games.pong.view.play.PlayView;

/**
 * The classic "Pong" game with different play modes.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class PongGameApp extends Application {

	public enum PlayMode {
		Player1_Player2, Player1_Computer, Computer_Player2, Computer_Computer
	}

	public static void main(String[] args) {
		launch(new PongGameApp());
	}

	private Score scorePlayerLeft, scorePlayerRight;
	public MenuView menuScene;
	public PlayView playScene;

	public PongGameApp() {
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
		entities.store(new Court(settings.width, settings.height));
		entities.store(new ScoreDisplay(getScorePlayerLeft(), getScorePlayerRight()));
		entities.store(new Ball(settings.height));
		entities.store(new AutoPaddleLeft(this));
		entities.store(new AutoPaddleRight(this));
		Paddle paddleLeft = new Paddle(this, VK_A, VK_Y);
		entities.store("paddleLeft", paddleLeft);
		Paddle paddleRight = new Paddle(this, VK_UP, VK_DOWN);
		entities.store("paddleRight", paddleRight);
		menuScene = new MenuView(this);
		playScene = new PlayView(this);
		setController(menuScene);
		setPlayMode(PlayMode.Player1_Player2);
	}

	public void setPlayMode(PlayMode playMode) {
		menuScene.setSelectedPlayMode(playMode);
	}

	public PlayMode getPlayMode() {
		return menuScene.getSelectedPlayMode();
	}

	public Score getScorePlayerLeft() {
		return scorePlayerLeft;
	}

	public Score getScorePlayerRight() {
		return scorePlayerRight;
	}
}