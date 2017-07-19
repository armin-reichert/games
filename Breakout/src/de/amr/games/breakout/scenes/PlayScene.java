package de.amr.games.breakout.scenes;

import static de.amr.games.breakout.Globals.BRICK_COLS;
import static de.amr.games.breakout.Globals.BRICK_ROWS;
import static de.amr.games.breakout.Globals.BRICK_WIDTH;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBat;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBrick;

import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.scene.Scene;
import de.amr.games.breakout.BreakoutGame;
import de.amr.games.breakout.entities.Ball;
import de.amr.games.breakout.entities.Bat;
import de.amr.games.breakout.entities.Brick;
import de.amr.games.breakout.entities.ScoreDisplay;

public class PlayScene extends Scene<BreakoutGame> {

	private Bat bat;
	private Ball ball;
	private Brick[][] bricks;
	private PlaySceneControl control;

	public PlayScene(BreakoutGame app) {
		super(app);
		control = new PlaySceneControl(this);
	}

	@Override
	public void init() {
		ball = app.entities.findAny(Ball.class);
		bat = app.entities.findAny(Bat.class);
		app.collisionHandler.registerStart(ball, bat, BallHitsBat);
		control.init();
	}

	@Override
	public void update() {
		for (Collision collision : app.collisionHandler.collisions()) {
			PlayEvent event = collision.getAppEvent();
			event.collision = collision;
			control.addInput(event);
		}
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(app.assets.image("Background/background.jpg"), 0, 0, getWidth(), getHeight(), null);
		for (int i = 0; i < BRICK_ROWS; ++i) {
			for (int j = 0; j < BRICK_COLS; ++j) {
				Brick brick = bricks[i][j];
				if (brick != null) {
					brick.draw(g);
				}
			}
		}
		app.entities.findAny(ScoreDisplay.class).draw(g);
		bat.draw(g);
		ball.draw(g);
	}

	void newBricks() {
		bricks = new Brick[BRICK_ROWS][BRICK_COLS];
		int padding = (getWidth() - BRICK_COLS * BRICK_WIDTH) / (BRICK_COLS + 1);
		int x = padding, y = padding;
		for (int i = 0; i < BRICK_ROWS; ++i) {
			for (int j = 0; j < BRICK_COLS; ++j) {
				int value = i == 0 ? 10 : 5;
				Brick.BrickColor color = i == 0 ? Brick.BrickColor.green : Brick.BrickColor.yellow;
				Brick brick = new Brick(app.assets, color, value);
				addBrick(brick, i, j);
				brick.tf.moveTo(x, y);
				x += padding + brick.getWidth();
			}
			x = padding;
			y += padding + 35;
		}
	}

	void addBrick(Brick brick, int i, int j) {
		bricks[i][j] = brick;
		app.collisionHandler.registerStart(ball, brick, BallHitsBrick);
	}

	void removeBrick(Brick brick) {
		app.collisionHandler.unregisterStart(ball, brick);
		for (int i = 0; i < BRICK_ROWS; ++i) {
			for (int j = 0; j < BRICK_COLS; ++j) {
				if (brick == bricks[i][j]) {
					bricks[i][j] = null;
				}
			}
		}
	}

	void updateEntities() {
		bat.update();
		ball.update();
	}

	void reset() {
		app.getScore().reset();
		resetBat();
		resetBall();
		newBricks();
	}

	void resetBall() {
		ball.hCenter(getWidth());
		ball.tf.setY(bat.tf.getY() - ball.getHeight());
		ball.tf.setVelocity(0, 0);
	}

	void resetBat() {
		bat.tf.moveTo(0, getHeight() - bat.getHeight());
		bat.hCenter(getWidth());
		bat.tf.setVelocity(0, 0);
		bat.setSpeed(5);
	}

	void bounceBallFromBat() {
		ball.tf.setVelocityY(-ball.tf.getVelocityY());
		ball.tf.setY(bat.tf.getY() - ball.getHeight());
		app.assets.sound("Sounds/plop.mp3").play();
	}

	void bounceBallFromBrick(Brick brick) {
		ball.tf.setY(brick.tf.getY() + brick.getHeight());
		ball.tf.setVelocityY(-ball.tf.getVelocityY());
	}

	void shootBall() {
		resetBat();
		resetBall();
		Random random = new Random();
		ball.tf.setVelocityX(2 + random.nextFloat() * 2);
		ball.tf.setVelocityY(-(8 + random.nextFloat() * 8));
		if (random.nextBoolean()) {
			ball.tf.setVelocityX(-ball.tf.getVelocityX());
		}
	}

	void addToScore(int points) {
		app.getScore().points += points;
	}

	void brickWasHit(Brick brick) {
		if (!brick.isCracked()) {
			bounceBallFromBrick(brick);
			brick.crack();
		} else {
			app.assets.sound("Sounds/point.mp3").play();
			addToScore(brick.getValue());
			removeBrick(brick);
		}
	}
}
