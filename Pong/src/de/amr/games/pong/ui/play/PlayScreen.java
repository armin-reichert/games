package de.amr.games.pong.ui.play;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.PULSE;
import static de.amr.games.pong.ui.play.PlayState.GAME_OVER;
import static de.amr.games.pong.ui.play.PlayState.INIT;
import static de.amr.games.pong.ui.play.PlayState.PLAYING;
import static de.amr.games.pong.ui.play.PlayState.SERVING;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_Y;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Random;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pong.PongGameApp;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.model.Game;
import de.amr.statemachine.StateMachine;

/**
 * The play view of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class PlayScreen implements View, Controller {

	private final Game game;
	private final PongGameApp app;
	private final StateMachine<PlayState, Object> fsm;
	private final Dimension size;

	public PlayScreen(PongGameApp app) {
		this.app = app;
		this.game = app.game;
		size = new Dimension(app.settings.width, app.settings.height);
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
			.state(PLAYING).onTick(this::updateEntities)
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

	private Court court;
	private Paddle paddleLeft;
	private Paddle paddleRight;
	private Ball ball;

	private void initEntities() {
		court = new Court(size);
		ball = new Ball(10, Color.YELLOW);
		ball.setCourtSize(size);
		switch (game.playMode) {
		case Computer_Computer:
			paddleLeft = new AutoPaddleLeft();
			paddleRight = new AutoPaddleRight();
			break;
		case Computer_Player2:
			paddleLeft = new AutoPaddleLeft();
			paddleRight = new Paddle(VK_UP, VK_DOWN);
			break;
		case Player1_Computer:
			paddleLeft = new Paddle(VK_A, VK_Y);
			paddleRight = new AutoPaddleRight();
			break;
		case Player1_Player2:
			paddleLeft = new Paddle(VK_A, VK_Y);
			paddleRight = new Paddle(VK_UP, VK_DOWN);
			break;
		}
		paddleLeft.setSize(15, 60);
		paddleLeft.setCourtSize(size);
		paddleLeft.setSpeed(5);
		paddleLeft.setBall(ball);
		paddleRight.setSize(15, 60);
		paddleRight.setCourtSize(size);
		paddleRight.setSpeed(5);
		paddleRight.setBall(ball);
	}

	private void updateEntities() {
		court.update();
		ball.update();
		paddleLeft.update();
		paddleRight.update();
	}

	@Override
	public void init() {
		initEntities();
		fsm.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_M)) {
			app.setController(app.menuViewController);
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
		g.translate(0, size.height / 2);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));
		g.drawString("" + game.scoreLeft, size.width / 4, 0);
		g.drawString("" + game.scoreRight, size.width * 3 / 4, 0);
		g.translate(0, -size.height / 2);
		if (leftPlayerWins()) {
			drawWinner(g, "Left Player wins!");
		} else if (rightPlayerWins()) {
			drawWinner(g, "Right Player wins!");
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	private void drawWinner(Graphics2D g, String text) {
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));
		int w = g.getFontMetrics().stringWidth(text);
		g.drawString(text, size.width / 2 - w / 2, size.height - 100);
	}

	private void resetPaddles() {
		paddleLeft.tf.setX(0);
		paddleLeft.centerVertically(size.height);
		paddleRight.tf.setX(size.width - paddleRight.tf.getWidth());
		paddleRight.centerVertically(size.height);
	}

	private void resetScores() {
		game.scoreLeft = 0;
		game.scoreRight = 0;
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
		return ball.tf.getX() > size.width;
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
		return game.scoreLeft == 11;
	}

	private boolean rightPlayerWins() {
		return game.scoreRight == 11;
	}

	private void assignPointToLeftPlayer() {
		game.scoreLeft += 1;
	}

	private void assignPointToRightPlayer() {
		game.scoreRight += 1;
	}
}