package de.amr.games.breakout.scenes;

import static de.amr.games.breakout.BreakoutGame.BALL_SIZE;
import static de.amr.games.breakout.BreakoutGame.BAT_HEIGHT;
import static de.amr.games.breakout.BreakoutGame.BAT_WIDTH;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBat;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBrick;
import static de.amr.games.breakout.scenes.PlayState.BallOut;
import static de.amr.games.breakout.scenes.PlayState.Initialized;
import static de.amr.games.breakout.scenes.PlayState.Playing;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.breakout.BreakoutGame;
import de.amr.games.breakout.entities.Ball;
import de.amr.games.breakout.entities.Bat;
import de.amr.games.breakout.entities.Brick;
import de.amr.games.breakout.entities.ScoreDisplay;

public class PlayScene extends Scene<BreakoutGame> {

	private final PlaySceneControl control;
	private ScoreDisplay score;
	private Bat bat;
	private Ball ball;
	private int rows;
	private int cols;
	private int brickWidth = 40;
	private int brickHeight = 16;
	private Brick[][] bricks;
	private Brick lastBrickHit;
	private Image background;

	private class PlaySceneControl extends StateMachine<PlayState, PlayEvent> {

		public PlaySceneControl() {
			super("BreakoutGameControl", PlayState.class, Initialized);

			// Initialized
			state(Initialized).entry = s -> reset();

			change(Initialized, Playing, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// Playing
			state(Playing).entry = s -> shootBall();

			state(Playing).update = s -> app.entities.all().forEach(GameEntity::update);

			changeOnInput(BallHitsBat, Playing, Playing, (s, t) -> bounceBallFromBat());

			changeOnInput(BallHitsBrick, Playing, Playing, (s, t) -> onBrickHit(lastBrickHit));

			change(Playing, BallOut, () -> ball.isOut());

			// BallOut
			state(BallOut).entry = s -> {
				resetBat();
				resetBall();
			};

			change(BallOut, Playing, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}
	}

	public PlayScene(BreakoutGame app) {
		super(app);
		control = new PlaySceneControl();
		control.setLogger(Application.LOG);
	}

	@Override
	public void init() {
		background = app.assets.image("Background/background.jpg").getScaledInstance(getWidth(), getHeight(),
				BufferedImage.SCALE_SMOOTH);
		score = app.entities.add(new ScoreDisplay(app));
		ball = app.entities.add(new Ball(app, BALL_SIZE));
		bat = app.entities.add(new Bat(app, BAT_WIDTH, BAT_HEIGHT));
		app.collisionHandler.registerStart(ball, bat, BallHitsBat);
		control.init();
	}

	@Override
	public void update() {
		for (Collision collision : app.collisionHandler.collisions()) {
			if (collision.getSecond() instanceof Brick) {
				lastBrickHit = (Brick) collision.getSecond();
			}
			control.addInput(collision.getAppEvent());
		}
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(background, 0, 0, null);
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				Brick brick = bricks[row][col];
				if (brick != null) {
					brick.draw(g);
				}
			}
		}
		score.draw(g);
		bat.draw(g);
		ball.draw(g);
	}

	private void newBricks() {
		cols = app.getWidth() / brickWidth;
		rows = 2;
		bricks = new Brick[rows][cols];
		int padding = 3;
		int x = padding, y = padding;
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				int value = row == 0 ? 10 : 5;
				Brick.Type color = row == 0 ? Brick.Type.green : Brick.Type.yellow;
				Brick brick = new Brick(app, brickWidth, brickHeight, color, value);
				addBrick(brick, row, col);
				brick.tf.moveTo(x, y);
				x += padding + brick.getWidth();
			}
			x = padding;
			y += brickHeight + padding;
		}
	}

	private void addBrick(Brick brick, int row, int col) {
		bricks[row][col] = brick;
		app.collisionHandler.registerStart(ball, brick, BallHitsBrick);
	}

	private void removeBrick(Brick brick) {
		app.collisionHandler.unregisterStart(ball, brick);
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				if (brick == bricks[row][col]) {
					bricks[row][col] = null;
				}
			}
		}
	}

	private void reset() {
		app.score.reset();
		resetBat();
		resetBall();
		newBricks();
	}

	private void resetBall() {
		ball.hCenter(getWidth());
		ball.tf.setY(bat.tf.getY() - ball.getHeight());
		ball.tf.setVelocity(0, 0);
	}

	private void resetBat() {
		bat.tf.moveTo(0, getHeight() - bat.getHeight());
		bat.hCenter(getWidth());
		bat.tf.setVelocity(0, 0);
		bat.speed = 8;
	}

	private void bounceBallFromBat() {
		ball.tf.setY(bat.tf.getY() - ball.getHeight());
		ball.tf.setVelocityY(-ball.tf.getVelocityY());
		app.assets.sound("Sounds/plop.mp3").play();
	}

	private void bounceBallFromBrick(Brick brick) {
		ball.tf.setY(brick.tf.getY() + brick.getHeight());
		ball.tf.setVelocityY(-ball.tf.getVelocityY());
	}

	private void shootBall() {
		resetBat();
		resetBall();
		Random random = new Random();
		ball.tf.setVelocityX(2 + random.nextFloat() * 2);
		ball.tf.setVelocityY(-(6 + random.nextFloat() * 6));
		if (random.nextBoolean()) {
			ball.tf.setVelocityX(-ball.tf.getVelocityX());
		}
	}

	private void onBrickHit(Brick brick) {
		if (!brick.isCracked()) {
			bounceBallFromBrick(brick);
			brick.crack();
		} else {
			app.assets.sound("Sounds/point.mp3").play();
			app.score.points += brick.getValue();
			removeBrick(brick);
		}
	}
}
