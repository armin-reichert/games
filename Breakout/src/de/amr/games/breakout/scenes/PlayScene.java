package de.amr.games.breakout.scenes;

import static de.amr.easy.game.Application.Assets;
import static de.amr.games.breakout.Globals.BRICK_COLS;
import static de.amr.games.breakout.Globals.BRICK_ROWS;
import static de.amr.games.breakout.Globals.BRICK_WIDTH;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBat;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBrick;

import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.fsm.FSM;
import de.amr.easy.game.Application;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.entity.collision.CollisionHandler;
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
	private FSM<PlayState, PlayEvent> control;

	public PlayScene(BreakoutGame game) {
		super(game);
		control = new PlaySceneControl(this);
	}

	@Override
	public void init() {
		ball = Application.Entities.findAny(Ball.class);
		bat = Application.Entities.findAny(Bat.class);
		CollisionHandler.detectCollisionStart(ball, bat, BallHitsBat);
		control.init();
	}

	@Override
	public void update() {
		for (Collision collision : CollisionHandler.collisions()) {
			PlayEvent event = collision.getAppEvent();
			event.collision = collision;
			control.enqueue(event);
		}
		control.run(PlayEvent.Tick);
	}

	@Override
	public void draw(Graphics2D g) {
		g.drawImage(Assets.image("Background/background.jpg"), 0, 0, getWidth(), getHeight(), null);
		for (int i = 0; i < BRICK_ROWS; ++i) {
			for (int j = 0; j < BRICK_COLS; ++j) {
				Brick brick = bricks[i][j];
				if (brick != null) {
					brick.draw(g);
				}
			}
		}
		Application.Entities.findAny(ScoreDisplay.class).draw(g);
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
		CollisionHandler.detectCollisionStart(ball, brick, BallHitsBrick);
	}

	void removeBrick(Brick brick) {
		CollisionHandler.ignoreCollisionStart(ball, brick);
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
		ball.tr.setVel(0, 0);
	}

	void resetBat() {
		bat.tr.moveTo(0, getHeight() - bat.getHeight());
		bat.centerHor(getWidth());
		bat.tr.setVel(0, 0);
		bat.setSpeed(5);
	}

	void bounceBallFromBat() {
		ball.tr.setVelY(-ball.tr.getVelY());
		ball.tr.setY(bat.tr.getY() - ball.getHeight());
		Assets.sound("Sounds/plop.mp3").play();
	}

	void bounceBallFromBrick(Brick brick) {
		ball.tr.setY(brick.tr.getY() + brick.getHeight());
		ball.tr.setVelY(-ball.tr.getVelY());
	}

	void shootBall() {
		resetBat();
		resetBall();
		Random random = new Random();
		ball.tr.setVelX(2 + random.nextFloat() * 2);
		ball.tr.setVelY(-(8 + random.nextFloat() * 8));
		if (random.nextBoolean()) {
			ball.tr.setVelX(-ball.tr.getVelX());
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
			Assets.sound("Sounds/point.mp3").play();
			addToScore(brick.getValue());
			removeBrick(brick);
		}
	}
}