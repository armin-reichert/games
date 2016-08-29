package de.amr.games.pong;

import static de.amr.games.pong.Globals.WINNING_SCORE;

import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.entities.ScoreDisplay;
import de.amr.games.pong.scenes.menu.Menu;
import de.amr.games.pong.scenes.play.PlayScene;

public class PongGame extends Application {

	public enum PlayMode {
		Player1_Player2, Player1_Computer, Computer_Player2, Computer_Computer
	}

	public static void main(String[] args) {
		Settings.title = "Pong";
		Settings.width = 640;
		Settings.height = 480;
		Settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
		launch(new PongGame());
	}

	private PlayMode playMode;
	private Score scorePlayerLeft, scorePlayerRight;

	public PongGame() {
		scorePlayerLeft = new Score(points -> points == WINNING_SCORE);
		scorePlayerRight = new Score(points -> points == WINNING_SCORE);
	}

	@Override
	protected void init() {
		setPlayMode(PlayMode.Player1_Player2);

		Assets.sound("plop.mp3");
		Assets.sound("plip.mp3");
		Assets.sound("out.mp3");

		Entities.add(new AutoPaddleLeft(this));
		Entities.add(new AutoPaddleRight(this));
		Entities.add(new Ball(getHeight()));
		Entities.add(new Court(this));
		Paddle paddleLeft = new Paddle(this, KeyEvent.VK_A, KeyEvent.VK_Y);
		paddleLeft.setName("paddleLeft");
		Entities.add(paddleLeft);
		Paddle paddleRight = new Paddle(this, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
		paddleRight.setName("paddleRight");
		Entities.add(paddleRight);
		Entities.add(new ScoreDisplay(getScorePlayerLeft(), getScorePlayerRight()));

		Views.add(new Menu(this));
		Views.add(new PlayScene(this));
		Views.show(Menu.class);
	}

	public void setPlayMode(PlayMode playMode) {
		this.playMode = playMode;
	}

	public PlayMode getPlayMode() {
		return playMode;
	}

	public Score getScorePlayerLeft() {
		return scorePlayerLeft;
	}

	public Score getScorePlayerRight() {
		return scorePlayerRight;
	}
}
