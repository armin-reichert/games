package de.amr.games.pong.scenes.play;

import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.pong.scenes.play.PlaySceneState.GameOver;
import static de.amr.games.pong.scenes.play.PlaySceneState.Initialized;
import static de.amr.games.pong.scenes.play.PlaySceneState.Playing;
import static de.amr.games.pong.scenes.play.PlaySceneState.Serving;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_SPACE;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.pong.PongGame;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.entities.ScoreDisplay;

/**
 * The play scene of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class PlayScene implements View, Controller {

	/**
	 * State machine for controlling the play scene.
	 */
	private StateMachine<PlaySceneState, PlaySceneEvent> createStateMachine() {

		StateMachine<PlaySceneState, PlaySceneEvent> fsm = new StateMachine<>("PongControl",
				PlaySceneState.class, Initialized);

		// Initialized

		fsm.change(Initialized, Serving, () -> true, t -> resetScores());

		// Serving

		fsm.state(Serving).entry = s -> {
			s.setDuration(Application.PULSE.secToTicks(2));
			prepareService();
		};

		fsm.changeOnTimeout(Serving, Playing, t -> serveBall());

		// Playing

		fsm.state(Playing).update = s -> app.entities.all().forEach(GameEntity::update);

		fsm.change(Playing, Playing, () -> leftPaddleHitsBall(), t -> bounceBallFromLeftPaddle());

		fsm.change(Playing, Playing, () -> rightPaddleHitsBall(), t -> bounceBallFromRightPaddle());

		fsm.change(Playing, Serving, () -> isBallOutLeft(), t -> assignPointToRightPlayer());

		fsm.change(Playing, Serving, () -> isBallOutRight(), t -> assignPointToLeftPlayer());

		fsm.change(Playing, GameOver, () -> leftPlayerWins() || rightPlayerWins());

		// GameOver

		fsm.change(GameOver, Initialized, () -> keyPressedOnce(VK_SPACE));

		return fsm;
	}

	private final PongGame app;
	private final int width;
	private final int height;
	private final StateMachine<PlaySceneState, PlaySceneEvent> control;
	private Court court;
	private Paddle paddleLeft;
	private Paddle paddleRight;
	private Ball ball;
	private ScoreDisplay score;

	public PlayScene(PongGame app) {
		this.app = app;
		this.width = app.settings.width;
		this.height = app.settings.height;
		control = createStateMachine();
		control.setLogger(Application.LOGGER);
	}

	@Override
	public void init() {
		court = app.entities.ofClass(Court.class).findFirst().get();
		switch (app.getPlayMode()) {
		case Computer_Computer:
			paddleLeft = app.entities.ofClass(AutoPaddleLeft.class).findFirst().get();
			paddleRight = app.entities.ofClass(AutoPaddleRight.class).findFirst().get();
			break;
		case Computer_Player2:
			paddleLeft = app.entities.ofClass(AutoPaddleLeft.class).findFirst().get();
			paddleRight = app.entities.ofName("paddleRight");
			break;
		case Player1_Computer:
			paddleLeft = app.entities.ofName("paddleLeft");
			paddleRight = app.entities.ofClass(AutoPaddleRight.class).findFirst().get();
			break;
		case Player1_Player2:
			paddleLeft = app.entities.ofName("paddleLeft");
			paddleRight = app.entities.ofName("paddleRight");
			break;
		}
		ball = app.entities.ofClass(Ball.class).findFirst().get();
		score = app.entities.ofClass(ScoreDisplay.class).findFirst().get();
		score.centerHorizontally(width);
		score.tf.setY(100);
		control.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(VK_C) && Keyboard.isControlDown()) {
			app.setController(app.menuScene);
		}
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// draw in z-order:
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
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));
		int w = g.getFontMetrics().stringWidth(text);
		g.drawString(text, width / 2 - w / 2, height - 100);
	}

	private void resetPaddles() {
		paddleLeft.tf.setX(0);
		paddleLeft.centerVertically(height);
		paddleRight.tf.setX(width - paddleRight.tf.getWidth());
		paddleRight.centerVertically(height);
	}

	private void resetScores() {
		app.getScorePlayerLeft().reset();
		app.getScorePlayerRight().reset();
	}

	private void prepareService() {
		resetPaddles();
		if (!isBallOutRight()) {
			ball.tf.moveTo(paddleLeft.tf.getX() + paddleLeft.tf.getWidth(),
					paddleLeft.tf.getY() + paddleLeft.tf.getHeight() / 2 - ball.tf.getHeight() / 2);
		} else {
			ball.tf.moveTo(paddleRight.tf.getX() - ball.tf.getWidth(),
					paddleRight.tf.getY() + paddleRight.tf.getHeight() / 2 - ball.tf.getHeight() / 2);
		}
		ball.tf.setVelocity(0, 0);
	}

	private void serveBall() {
		Random rnd = new Random();
		int ballSpeed = 10;
		ball.tf.setVelocityX(isBallOutRight() ? -ballSpeed : ballSpeed);
		ball.tf.setVelocityY((ballSpeed / 4) + (rnd.nextFloat() * ballSpeed / 4));
		if (rnd.nextBoolean()) {
			ball.tf.setVelocityY(-ball.tf.getVelocityY());
		}
	}

	private boolean isBallOutLeft() {
		return ball.tf.getX() + ball.tf.getWidth() < 0;
	}

	private boolean isBallOutRight() {
		return ball.tf.getX() > width;
	}

	private boolean leftPaddleHitsBall() {
		return ball.tf.getVelocityX() <= 0 && paddleLeft.hitsBall(ball);
	}

	private boolean rightPaddleHitsBall() {
		return ball.tf.getVelocityX() >= 0 && paddleRight.hitsBall(ball);
	}

	private void bounceBallFromLeftPaddle() {
		ball.tf.setVelocityX(-ball.tf.getVelocityX());
		Assets.sound("plop.mp3").play();
	}

	private void bounceBallFromRightPaddle() {
		ball.tf.setVelocityX(-ball.tf.getVelocityX());
		Assets.sound("plip.mp3").play();
	}

	private boolean leftPlayerWins() {
		return app.getScorePlayerLeft().isWinning();
	}

	private boolean rightPlayerWins() {
		return app.getScorePlayerRight().isWinning();
	}

	private void assignPointToRightPlayer() {
		app.getScorePlayerRight().points++;
	}

	private void assignPointToLeftPlayer() {
		app.getScorePlayerLeft().points++;
	}
}
