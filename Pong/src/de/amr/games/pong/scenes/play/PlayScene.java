package de.amr.games.pong.scenes.play;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.PULSE;
import static de.amr.games.pong.scenes.play.PlayState.GAME_OVER;
import static de.amr.games.pong.scenes.play.PlayState.INIT;
import static de.amr.games.pong.scenes.play.PlayState.PLAYING;
import static de.amr.games.pong.scenes.play.PlayState.SERVING;
import static java.awt.event.KeyEvent.VK_C;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Random;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pong.PongGameApp;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.entities.ScoreDisplay;
import de.amr.statemachine.StateMachine;

/**
 * The play scene of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class PlayScene implements View, Controller {

	private final PongGameApp app;
	private final StateMachine<PlayState, Object> fsm;
	private final int width;
	private final int height;

	private Court court;
	private Paddle paddleLeft;
	private Paddle paddleRight;
	private Ball ball;
	private ScoreDisplay score;

	public PlayScene(PongGameApp app) {
		this.app = app;
		width = app.settings.width;
		height = app.settings.height;
		fsm = createStateMachine();
		fsm.traceTo(LOGGER, PULSE::getFrequency);
	}

	private StateMachine<PlayState, Object> createStateMachine() {
		return
		//@formatter:off
		StateMachine.define(PlayState.class, Object.class)
		.description("Pong")	
		.initialState(INIT)
	
		.states()
		.state(INIT).onEntry(this::resetPaddles)
		.state(SERVING).timeoutAfter(() -> PULSE.secToTicks(2)).onEntry(this::prepareService)
		.state(PLAYING).onTick(()-> app.entities.all().forEach(GameEntity::update))
		.state(GAME_OVER)
			
		.transitions()
		.when(INIT).then(SERVING).act(this::resetScores)
		.when(SERVING).then(PLAYING).onTimeout().act(this::serveBall)
		.stay(PLAYING).condition(this::leftPaddleHitsBall).act(this::returnBallWithLeftPaddle)
		.stay(PLAYING).condition(this::rightPaddleHitsBall).act(this::returnBallWithRightPaddle)
		.when(PLAYING).then(SERVING).condition(this::isBallOutLeft).act(this::assignPointToRightPlayer)
		.when(PLAYING).then(SERVING).condition(this::isBallOutRight).act(this::assignPointToLeftPlayer)
		.when(PLAYING).then(GAME_OVER).condition(() -> leftPlayerWins() || rightPlayerWins())
		.when(GAME_OVER).then(INIT).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
		.endStateMachine();
		//@formatter:on
	}

	@Override
	public void init() {
		initEntities();
		fsm.init();
	}

	private void initEntities() {
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
		score.tf.setY(100);
		score.centerHorizontally(width);
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(VK_C) && Keyboard.isControlDown()) {
			app.setController(app.menuScene);
		}
		fsm.update();
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
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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

	private void returnBallWithLeftPaddle() {
		ball.tf.setVelocityX(-ball.tf.getVelocityX());
		Assets.sound("plop.mp3").play();
	}

	private void returnBallWithRightPaddle() {
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