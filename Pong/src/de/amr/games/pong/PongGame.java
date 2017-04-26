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

	public static final PongGame Game = new PongGame();

	public static void main(String[] args) {
		Game.settings.title = "Pong";
		Game.settings.width = 640;
		Game.settings.height = 480;
		Game.settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
		launch(Game);
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

		assets.sound("plop.mp3");
		assets.sound("plip.mp3");
		assets.sound("out.mp3");

		entities.add(new AutoPaddleLeft(this));
		entities.add(new AutoPaddleRight(this));
		entities.add(new Ball(getHeight()));
		entities.add(new Court(this));
		Paddle paddleLeft = new Paddle(this, KeyEvent.VK_A, KeyEvent.VK_Y);
		paddleLeft.setName("paddleLeft");
		entities.add(paddleLeft);
		Paddle paddleRight = new Paddle(this, KeyEvent.VK_UP, KeyEvent.VK_DOWN);
		paddleRight.setName("paddleRight");
		entities.add(paddleRight);
		entities.add(new ScoreDisplay(getScorePlayerLeft(), getScorePlayerRight()));

		views.add(new Menu(this));
		views.add(new PlayScene(this));
		views.show(Menu.class);
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
