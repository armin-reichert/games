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
import java.util.stream.IntStream;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.pong.entities.AutoPaddleLeft;
import de.amr.games.pong.entities.AutoPaddleRight;
import de.amr.games.pong.entities.Ball;
import de.amr.games.pong.entities.Court;
import de.amr.games.pong.entities.Paddle;
import de.amr.games.pong.model.Game;
import de.amr.games.pong.ui.ScreenManager;
import de.amr.statemachine.StateMachine;

/**
 * The play view of the "Pong" game.
 * 
 * @author Armin Reichert
 */
public class PlayScreen implements View, Controller {

	private final ScreenManager screenManager;
	private final Game game;
	private final StateMachine<PlayState, Object> fsm;
	private final Dimension size;

	public PlayScreen(ScreenManager screenManager, Game game, Dimension size) {
		this.screenManager = screenManager;
		this.game = game;
		this.size = size;
		fsm = createStateMachine();
		fsm.traceTo(Application.LOGGER, app().clock::getFrequency);
	}

	private StateMachine<PlayState, Object> createStateMachine() {
		return
		//@formatter:off
		StateMachine.beginStateMachine(PlayState.class, Object.class)
			.description("Pong")	
			.initialState(INIT)
	
		.states()
			.state(INIT).onEntry(this::initEntities)
			.state(SERVING).timeoutAfter(() -> app().clock.sec(2)).onEntry(this::prepareService)
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
	private Paddle paddle[] = new Paddle[2];
	private Ball ball;

	private void initEntities() {
		court = new Court(size);
		ball = new Ball(12);
		ball.setColor(Color.YELLOW);
		ball.setCourtSize(size);
		switch (game.playMode) {
		case Computer_Computer:
			paddle[0] = new AutoPaddleLeft();
			paddle[1] = new AutoPaddleRight();
			break;
		case Computer_Player2:
			paddle[0] = new AutoPaddleLeft();
			paddle[1] = new Paddle(VK_UP, VK_DOWN);
			break;
		case Player1_Computer:
			paddle[0] = new Paddle(VK_A, VK_Y);
			paddle[1] = new AutoPaddleRight();
			break;
		case Player1_Player2:
			paddle[0] = new Paddle(VK_A, VK_Y);
			paddle[1] = new Paddle(VK_UP, VK_DOWN);
			break;
		}
		IntStream.rangeClosed(0, 1).forEach(i -> {
			paddle[i].setSize(15, 60);
			paddle[i].setCourtSize(size);
			paddle[i].setSpeed(5);
			paddle[i].setColor(Color.LIGHT_GRAY);
			paddle[i].setBall(ball);
		});
		resetPaddles();
	}

	private void updateEntities() {
		court.update();
		ball.update();
		paddle[0].update();
		paddle[1].update();
	}

	@Override
	public void init() {
		fsm.init();
	}

	@Override
	public void update() {
		if (Keyboard.keyPressedOnce(KeyEvent.VK_M)) {
			screenManager.selectMenuScreen();
		}
		fsm.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		court.draw(g);
		paddle[0].draw(g);
		paddle[1].draw(g);
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
		paddle[0].tf.setX(0);
		paddle[0].tf.centerY(size.height);
		paddle[1].tf.setX(size.width - paddle[1].tf.getWidth());
		paddle[1].tf.centerY(size.height);
	}

	private void resetScores() {
		game.scoreLeft = 0;
		game.scoreRight = 0;
	}

	private void prepareService() {
		resetPaddles();
		if (!isBallOutRight()) {
			ball.tf.setPosition(paddle[0].tf.getX() + paddle[0].tf.getWidth(),
					paddle[0].tf.getY() + paddle[0].tf.getHeight() / 2 - ball.tf.getHeight() / 2);
		} else {
			ball.tf.setPosition(paddle[1].tf.getX() - ball.tf.getWidth(),
					paddle[1].tf.getY() + paddle[1].tf.getHeight() / 2 - ball.tf.getHeight() / 2);
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
		return ball.tf.getVelocityX() <= 0 && paddle[0].collidesWith(ball);
	}

	private boolean rightPaddleHitsBall() {
		return ball.tf.getVelocityX() >= 0 && paddle[1].collidesWith(ball);
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