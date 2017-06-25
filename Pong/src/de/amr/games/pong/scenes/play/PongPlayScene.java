package de.amr.games.pong.scenes.play;

import static de.amr.games.pong.PongGlobals.BALL_SPEED;
import static de.amr.games.pong.PongGlobals.FONT;
import static de.amr.games.pong.PongGlobals.WINNING_SCORE;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_CONTROL;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;
import java.util.logging.Logger;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.games.pong.PongGame;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.entities.ScoreDisplay;
import de.amr.games.pong.scenes.menu.Menu;

public class PongPlayScene extends Scene<PongGame> {

	private final PlaySceneControl control;
	private Court court;
	private Paddle paddleLeft;
	private Paddle paddleRight;
	private Ball ball;
	private ScoreDisplay score;

	public PongPlayScene(PongGame game) {
		super(game);
		control = new PlaySceneControl(this);
		control.setLogger(Logger.getGlobal());
	}

	@Override
	public void init() {
		court = app.entities.findAny(Court.class);
		switch (getApp().getPlayMode()) {
		case Computer_Computer:
			paddleLeft = app.entities.findAny(AutoPaddleLeft.class);
			paddleRight = app.entities.findAny(AutoPaddleRight.class);
			break;
		case Computer_Player2:
			paddleLeft = app.entities.findAny(AutoPaddleLeft.class);
			paddleRight = app.entities.findByName(Paddle.class, "paddleRight");
			break;
		case Player1_Computer:
			paddleLeft = app.entities.findByName(Paddle.class, "paddleLeft");
			paddleRight = app.entities.findAny(AutoPaddleRight.class);
			break;
		case Player1_Player2:
			paddleLeft = app.entities.findByName(Paddle.class, "paddleLeft");
			paddleRight = app.entities.findByName(Paddle.class, "paddleRight");
			break;
		}
		ball = app.entities.findAny(Ball.class);
		score = app.entities.findAny(ScoreDisplay.class);
		score.centerHor(getWidth());
		score.tr.setY(100);
		control.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(VK_CONTROL, VK_C)) {
			app.views.show(Menu.class);
		}
		control.update();
	}

	void updateEntities() {
		ball.update();
		paddleLeft.update();
		paddleRight.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		court.draw(g);
		paddleLeft.draw(g);
		paddleRight.draw(g);
		ball.draw(g);
		score.draw(g);
		if (leftPlayerWins()) {
			drawWinner(g, "Left wins!");
		} else if (rightPlayerWins()) {
			drawWinner(g, "Right wins!");
		}
	}

	private void drawWinner(Graphics2D g, String text) {
		g.setFont(FONT);
		int w = g.getFontMetrics().stringWidth(text);
		g.drawString(text, getWidth() / 2 - w / 2, getHeight() - 100);
	}

	private void resetPaddles() {
		paddleLeft.tr.setX(0);
		paddleLeft.centerVert(getHeight());
		paddleRight.tr.setX(getWidth() - paddleRight.getWidth());
		paddleRight.centerVert(getHeight());
	}

	void resetScores() {
		getApp().getScorePlayerLeft().reset();
		getApp().getScorePlayerRight().reset();
	}

	void prepareBall() {
		resetPaddles();
		if (!isBallOutRight()) {
			ball.tr.moveTo(paddleLeft.tr.getX() + paddleLeft.getWidth(),
					paddleLeft.tr.getY() + paddleLeft.getHeight() / 2 - ball.getHeight() / 2);
		} else {
			ball.tr.moveTo(paddleRight.tr.getX() - ball.getWidth(),
					paddleRight.tr.getY() + paddleRight.getHeight() / 2 - ball.getHeight() / 2);
		}
		ball.tr.setVel(0, 0);
	}

	void shootBall() {
		Random rnd = new Random();
		ball.tr.setVelX(isBallOutRight() ? -BALL_SPEED : BALL_SPEED);
		ball.tr.setVelY((BALL_SPEED / 4) + (rnd.nextFloat() * BALL_SPEED / 4));
		if (rnd.nextBoolean()) {
			ball.tr.setVelY(-ball.tr.getVelY());
		}
	}

	boolean isBallOutLeft() {
		return ball.tr.getX() + ball.getWidth() < 0;
	}

	boolean isBallOutRight() {
		return ball.tr.getX() > getWidth();
	}

	boolean leftPaddleHitsBall() {
		return ball.tr.getVelX() <= 0 && paddleLeft.hitsBall(ball);
	}

	boolean rightPaddleHitsBall() {
		return ball.tr.getVelX() >= 0 && paddleRight.hitsBall(ball);
	}

	void bounceBallFromLeftPaddle() {
		ball.tr.setX(paddleLeft.tr.getX() + paddleLeft.getWidth() + 1);
		ball.tr.setVelX(-ball.tr.getVelX());
		app.assets.sound("plop.mp3").play();
	}

	void bounceBallFromRightPaddle() {
		ball.tr.setX(paddleRight.tr.getX() - ball.getWidth() - 1);
		ball.tr.setVelX(-ball.tr.getVelX());
		app.assets.sound("plip.mp3").play();
	}

	boolean leftPlayerWins() {
		return getApp().getScorePlayerLeft().points == WINNING_SCORE;
	}

	boolean rightPlayerWins() {
		return getApp().getScorePlayerRight().points == WINNING_SCORE;
	}

	void assignPointToRightPlayer() {
		getApp().getScorePlayerRight().points++;
	}

	void assignPointToLeftPlayer() {
		getApp().getScorePlayerLeft().points++;
	}
}