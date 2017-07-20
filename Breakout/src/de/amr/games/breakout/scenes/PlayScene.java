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
	private int brickWidth = 60;
	private int brickHeight = brickWidth / 4;
	private int brickPadding = brickHeight / 2;
	private int numBricks;
	private Brick[][] bricks;
	private Image background;

	private class PlaySceneControl extends StateMachine<PlayState, PlayEvent> {

		public PlaySceneControl() {
			super("BreakoutGameControl", PlayState.class, Initialized);

			// Initialized
			state(Initialized).entry = s -> reset();

			change(Initialized, Playing, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

			// Playing
			state(Playing).entry = s -> shootBall();

			state(Playing).update = s -> {
				if (numBricks == 0) {
					resetBat();
					resetBall();
					newBricks();
				}
				app.entities.all().forEach(GameEntity::update);
			};

			changeOnInput(BallHitsBat, Playing, Playing, (e, s, t) -> bounceBallFromBat());

			changeOnInput(BallHitsBrick, Playing, Playing, (e, s, t) -> {
				onBrickHit((Brick) e.userData);
			});

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
		handleCollisions();
		control.update();
	}

	private void handleCollisions() {
		for (Collision coll : app.collisionHandler.collisions()) {
			PlayEvent event = coll.getAppEvent();
			if (coll.getSecond() instanceof Brick) {
				event.userData = coll.getSecond();
			}
			control.addInput(event);
		}
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
		Brick.Type[] types = Brick.Type.values();
		cols = (getWidth() - brickPadding) / (brickWidth + brickPadding) - 2;
		rows = types.length;
		numBricks = 0;
		bricks = new Brick[rows][cols];
		int startX = (getWidth() - cols * (brickWidth + brickPadding)) / 2;
		int startY = brickHeight * 5;
		int x = startX, y = startY;
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				Brick.Type color = types[row];
				int value = 5 * (types.length - row);
				Brick brick = new Brick(app, brickWidth, brickHeight, color, value);
				brick.tf.moveTo(x, y);
				addBrick(brick, row, col);
				x += brickWidth + brickPadding;
			}
			x = startX;
			y += brickHeight + brickPadding;
		}
	}

	private void addBrick(Brick brick, int row, int col) {
		bricks[row][col] = brick;
		numBricks += 1;
		app.collisionHandler.registerStart(ball, brick, BallHitsBrick);
	}

	private void removeBrick(Brick brick) {
		app.collisionHandler.unregisterStart(ball, brick);
		for (int row = 0; row < rows; ++row) {
			for (int col = 0; col < cols; ++col) {
				if (brick == bricks[row][col]) {
					bricks[row][col] = null;
					numBricks -= 1;
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
		bat.speed = 12;
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
		ball.tf.setVelocityX(4 + random.nextFloat() * 4);
		ball.tf.setVelocityY(-(8 + random.nextFloat() * 8));
		if (random.nextBoolean()) {
			ball.tf.setVelocityX(-ball.tf.getVelocityX());
		}
	}

	private void onBrickHit(Brick brick) {
		if (!brick.isDamaged()) {
			bounceBallFromBrick(brick);
			brick.damage();
		} else {
			app.assets.sound("Sounds/point.mp3").play();
			app.score.points += brick.getValue();
			removeBrick(brick);
		}
	}
}
