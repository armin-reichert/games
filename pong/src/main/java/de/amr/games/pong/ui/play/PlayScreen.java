package de.amr.games.pong.ui.play;

import static de.amr.easy.game.Application.app;
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
import de.amr.easy.game.controller.Lifecycle;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.View;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.KeyControlledPaddle;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.model.PongGame;
import de.amr.games.pong.ui.ScreenManager;
import de.amr.statemachine.core.StateMachine;

/**
 * The play screen of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class PlayScreen extends StateMachine<PlayState, Void> implements View, Lifecycle {

	private final ScreenManager screenManager;
	private final PongGame game;
	private final Dimension courtSize;
	private final Paddle[] paddles = new Paddle[2];
	private Court court;
	private Ball ball;

	public PlayScreen(ScreenManager screenManager, PongGame game, Dimension courtSize) {
		super(PlayState.class);
		this.screenManager = screenManager;
		this.game = game;
		this.courtSize = courtSize;
		//@formatter:off
		beginStateMachine()
			.description("Pong")	
			.initialState(INIT)
	
		.states()
			.state(INIT).onEntry(this::initEntities)
			.state(SERVING).timeoutAfter(() -> app().clock().sec(2)).onEntry(this::prepareService)
			.state(PLAYING).onTick(this::updateEntities)
			.state(GAME_OVER)
			
		.transitions()
			.when(INIT).then(SERVING).act(this::resetScores)
			.when(SERVING).then(PLAYING).onTimeout().act(this::serveBall)
			.stay(PLAYING).condition(this::leftPaddleHitsBall).act(this::returnBallWithLeftPaddle)
			.stay(PLAYING).condition(this::rightPaddleHitsBall).act(this::returnBallWithRightPaddle)
			.when(PLAYING).then(SERVING).condition(this::isBallOutLeft).act(this::handleBallOutLeft)
			.when(PLAYING).then(SERVING).condition(this::isBallOutRight).act(this::handleBallOutRight)
			.when(PLAYING).then(GAME_OVER).condition(() -> leftPlayerWins() || rightPlayerWins())
			.when(GAME_OVER).then(INIT).condition(() -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))

		.endStateMachine();
		//@formatter:on
	}

	private void initEntities() {
		court = new Court();
		court.tf.width = (courtSize.width);
		court.tf.height = (courtSize.height);
		court.floorColor = Color.BLACK;
		court.lineColor = Color.WHITE;
		court.lineWidth = 5;

		ball = new Ball();
		ball.tf.width = (12);
		ball.tf.height = (12);
		ball.color = Color.YELLOW;
		ball.maxY = courtSize.height;

		switch (game.playMode) {
		case Computer_Computer:
			paddles[0] = new AutoPaddleLeft();
			paddles[1] = new AutoPaddleRight();
			break;
		case Computer_Player2:
			paddles[0] = new AutoPaddleLeft();
			paddles[1] = new KeyControlledPaddle(VK_UP, VK_DOWN);
			break;
		case Player1_Computer:
			paddles[0] = new KeyControlledPaddle(VK_A, VK_Y);
			paddles[1] = new AutoPaddleRight();
			break;
		case Player1_Player2:
			paddles[0] = new KeyControlledPaddle(VK_A, VK_Y);
			paddles[1] = new KeyControlledPaddle(VK_UP, VK_DOWN);
			break;
		default:
			throw new IllegalArgumentException("Unknown play mode: " + game.playMode);
		}
		for (Paddle paddle : paddles) {
			paddle.tf.width = (15);
			paddle.tf.height = (60);
			paddle.maxX = courtSize.width;
			paddle.maxY = courtSize.height;
			paddle.speed = 5;
			paddle.color = Color.LIGHT_GRAY;
			paddle.ball = ball;
		}
		;
		resetPaddles();
	}

	private void updateEntities() {
		ball.update();
		for (Paddle paddle : paddles) {
			paddle.update();
		}
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_M)) {
			screenManager.selectMenuScreen();
		}
		super.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		court.draw(g);
		paddles[0].draw(g);
		paddles[1].draw(g);
		ball.draw(g);
		g.translate(0, courtSize.height / 2);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial Black", Font.PLAIN, 28));
		g.drawString("" + game.scoreLeft, courtSize.width / 4, 0);
		g.drawString("" + game.scoreRight, courtSize.width * 3 / 4, 0);
		g.translate(0, -courtSize.height / 2);
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
		g.drawString(text, courtSize.width / 2 - w / 2, courtSize.height - 100);
	}

	private void resetPaddles() {
		paddles[0].tf.x = (0);
		paddles[0].tf.centerVertically(0, courtSize.height);
		paddles[1].tf.x = (courtSize.width - paddles[1].tf.width);
		paddles[1].tf.centerVertically(0, courtSize.height);
	}

	private void resetScores() {
		game.scoreLeft = 0;
		game.scoreRight = 0;
	}

	private void prepareService() {
		resetPaddles();
		if (!isBallOutRight()) {
			ball.tf.setPosition(paddles[0].tf.x + paddles[0].tf.width,
					paddles[0].tf.y + paddles[0].tf.height / 2 - ball.tf.height / 2);
		} else {
			ball.tf.setPosition(paddles[1].tf.x - ball.tf.width,
					paddles[1].tf.y + paddles[1].tf.height / 2 - ball.tf.height / 2);
		}
		ball.tf.setVelocity(0, 0);
	}

	private void serveBall() {
		Random rnd = new Random();
		int ballSpeed = 10;
		ball.tf.vx = isBallOutRight() ? -ballSpeed : ballSpeed;
		ball.tf.vy = (ballSpeed / 4) + (rnd.nextFloat() * ballSpeed / 4);
		if (rnd.nextBoolean()) {
			ball.tf.vy *= -1;
		}
	}

	private boolean isBallOutLeft() {
		return ball.tf.x + ball.tf.width < 0;
	}

	private boolean isBallOutRight() {
		return ball.tf.x > courtSize.width;
	}

	private boolean leftPaddleHitsBall() {
		return ball.tf.vx <= 0 && paddles[0].collidesWith(ball);
	}

	private boolean rightPaddleHitsBall() {
		return ball.tf.vx >= 0 && paddles[1].collidesWith(ball);
	}

	private void returnBallWithLeftPaddle() {
		float rightEdge = paddles[0].tf.x + paddles[0].tf.width;
		if (ball.tf.x < rightEdge) {
			ball.tf.x = (rightEdge);
		}
		ball.tf.vx *= -1;
		playSoundPlop();
	}

	private void returnBallWithRightPaddle() {
		ball.tf.vx *= -1;
		playSoundPlip();
	}

	private boolean leftPlayerWins() {
		return game.scoreLeft == 11;
	}

	private boolean rightPlayerWins() {
		return game.scoreRight == 11;
	}

	private void handleBallOutRight() {
		game.scoreLeft += 1;
		playSoundOut();
	}

	private void handleBallOutLeft() {
		game.scoreRight += 1;
		playSoundOut();
	}

	private void playSoundPlip() {
		Assets.sound("plip.mp3").play();
	}

	private void playSoundPlop() {
		Assets.sound("plop.mp3").play();
	}

	private void playSoundOut() {
		Assets.sound("out.mp3").play();
	}
}