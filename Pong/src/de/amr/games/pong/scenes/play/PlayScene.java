package de.amr.games.pong.scenes.play;

import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.pong.scenes.play.PlaySceneState.GameOver;
import static de.amr.games.pong.scenes.play.PlaySceneState.Initialized;
import static de.amr.games.pong.scenes.play.PlaySceneState.Playing;
import static de.amr.games.pong.scenes.play.PlaySceneState.Serving;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_SPACE;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.scene.ActiveScene;
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
public class PlayScene implements ActiveScene<Graphics2D> {

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
			s.setDuration(app.pulse.secToTicks(2));
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
	private final StateMachine<PlaySceneState, PlaySceneEvent> control;
	private Court court;
	private Paddle paddleLeft;
	private Paddle paddleRight;
	private Ball ball;
	private ScoreDisplay score;

	public PlayScene(PongGame app) {
		this.app = app;
		control = createStateMachine();
		control.setLogger(Application.LOG);
	}

	@Override
	public int getWidth() {
		return app.getWidth();
	}

	@Override
	public int getHeight() {
		return app.getHeight();
	}

	@Override
	public void init() {
		court = app.entities.findAny(Court.class);
		switch (app.getPlayMode()) {
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
		score.hCenter(getWidth());
		score.tf.setY(100);
		control.init();
	}

	@Override
	public void update() {
		if (keyPressedOnce(VK_CONTROL, VK_C)) {
			app.select(app.menuScene);
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
		g.drawString(text, getWidth() / 2 - w / 2, getHeight() - 100);
	}

	private void resetPaddles() {
		paddleLeft.tf.setX(0);
		paddleLeft.vCenter(getHeight());
		paddleRight.tf.setX(getWidth() - paddleRight.getWidth());
		paddleRight.vCenter(getHeight());
	}

	private void resetScores() {
		app.getScorePlayerLeft().reset();
		app.getScorePlayerRight().reset();
	}

	private void prepareService() {
		resetPaddles();
		if (!isBallOutRight()) {
			ball.tf.moveTo(paddleLeft.tf.getX() + paddleLeft.getWidth(),
					paddleLeft.tf.getY() + paddleLeft.getHeight() / 2 - ball.getHeight() / 2);
		} else {
			ball.tf.moveTo(paddleRight.tf.getX() - ball.getWidth(),
					paddleRight.tf.getY() + paddleRight.getHeight() / 2 - ball.getHeight() / 2);
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
		return ball.tf.getX() + ball.getWidth() < 0;
	}

	private boolean isBallOutRight() {
		return ball.tf.getX() > getWidth();
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
