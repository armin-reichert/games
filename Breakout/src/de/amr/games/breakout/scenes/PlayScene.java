package de.amr.games.breakout.scenes;

import static de.amr.games.breakout.BreakoutGame.Game;
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

	public PlayScene(BreakoutGame game) {
		super(game);
		control = new PlaySceneControl(this);
	}

	@Override
	public void init() {
		ball = Game.entities.findAny(Ball.class);
		bat = Game.entities.findAny(Bat.class);
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
		g.drawImage(Game.assets.image("Background/background.jpg"), 0, 0, getWidth(), getHeight(), null);
		for (int i = 0; i < BRICK_ROWS; ++i) {
			for (int j = 0; j < BRICK_COLS; ++j) {
				Brick brick = bricks[i][j];
				if (brick != null) {
					brick.draw(g);
				}
			}
		}
		Game.entities.findAny(ScoreDisplay.class).draw(g);
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
				Brick brick = new Brick(color, value);
				addBrick(brick, i, j);
				brick.tr.moveTo(x, y);
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
		getApp().getScore().reset();
		resetBat();
		resetBall();
		newBricks();
	}

	void resetBall() {
		ball.centerHor(getWidth());
		ball.tr.setY(bat.tr.getY() - ball.getHeight());
		ball.tr.setVelocity(0, 0);
	}

	void resetBat() {
		bat.tr.moveTo(0, getHeight() - bat.getHeight());
		bat.centerHor(getWidth());
		bat.tr.setVelocity(0, 0);
		bat.setSpeed(5);
	}

	void bounceBallFromBat() {
		ball.tr.setVelocityY(-ball.tr.getVelocityY());
		ball.tr.setY(bat.tr.getY() - ball.getHeight());
		Game.assets.sound("Sounds/plop.mp3").play();
	}

	void bounceBallFromBrick(Brick brick) {
		ball.tr.setY(brick.tr.getY() + brick.getHeight());
		ball.tr.setVelocityY(-ball.tr.getVelocityY());
	}

	void shootBall() {
		resetBat();
		resetBall();
		Random random = new Random();
		ball.tr.setVelocityX(2 + random.nextFloat() * 2);
		ball.tr.setVelocityY(-(8 + random.nextFloat() * 8));
		if (random.nextBoolean()) {
			ball.tr.setVelocityX(-ball.tr.getVelocityX());
		}
	}

	void addToScore(int points) {
		getApp().getScore().points += points;
	}

	void brickWasHit(Brick brick) {
		if (!brick.isCracked()) {
			bounceBallFromBrick(brick);
			brick.crack();
		} else {
			Game.assets.sound("Sounds/point.mp3").play();
			addToScore(brick.getValue());
			removeBrick(brick);
		}
	}
}
