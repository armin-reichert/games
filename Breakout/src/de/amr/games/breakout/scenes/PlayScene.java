package de.amr.games.breakout.scenes;

import static de.amr.easy.game.input.Keyboard.keyPressedOnce;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBat;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBrick;
import static de.amr.games.breakout.scenes.PlayState.BallOut;
import static de.amr.games.breakout.scenes.PlayState.Initialized;
import static de.amr.games.breakout.scenes.PlayState.Playing;
import static java.awt.event.KeyEvent.VK_SPACE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.breakout.BreakoutGame;
import de.amr.games.breakout.entities.Ball;
import de.amr.games.breakout.entities.Bat;
import de.amr.games.breakout.entities.Brick;

public class PlayScene extends Scene<BreakoutGame> {

	private final PlaySceneControl control;
	private Bat bat;
	private Ball ball;
	private int rows;
	private int cols;
	private int points;

	public PlayScene(BreakoutGame app) {
		super(app);
		control = new PlaySceneControl();
		control.setLogger(Application.LOG);
	}

	private class PlaySceneControl extends StateMachine<PlayState, PlayEvent> {

		public PlaySceneControl() {
			super("BreakoutGameControl", PlayState.class, Initialized);

			// Initialized
			state(Initialized).entry = s -> reset();

			change(Initialized, Playing, () -> keyPressedOnce(VK_SPACE));

			// Playing
			state(Playing).entry = s -> shootBall();

			state(Playing).update = s -> play();

			changeOnInput(BallHitsBat, Playing, Playing, (e, s, t) -> bounceBallFromBat());

			changeOnInput(BallHitsBrick, Playing, Playing, (e, s, t) -> onBrickHit(e));

			change(Playing, BallOut, () -> ball.isOut());

			// BallOut
			state(BallOut).entry = s -> resetBatAndBall();

			change(BallOut, Playing, () -> keyPressedOnce(VK_SPACE));
		}
	}

	@Override
	public void init() {
		Image background = Assets.image("background.jpg").getScaledInstance(getWidth(), getHeight(),
				BufferedImage.SCALE_SMOOTH);
		setBgImage(background);
		ball = app.entities.add(new Ball(app, app.settings.get("ball_size")));
		bat = app.entities.add(new Bat(app.settings.get("bat_width"), app.settings.get("bat_height"), getWidth()));
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
			switch (event) {
			case BallHitsBrick:
				event.setUserData(coll.getSecond());
				break;
			case BallHitsBat:
				break;
			}
			control.addInput(event);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		drawScore(g);
	}

	private void drawScore(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.RED);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(String.valueOf(points), g);
		g.drawString(String.valueOf(points), (int) (getWidth() - bounds.getWidth()) / 2, getHeight() * 3 / 4);
	}

	private void play() {
		if (app.entities.filter(Brick.class).count() == 0) {
			resetBatAndBall();
			newBricks();
		}
		app.entities.all().forEach(GameEntity::update);
	}

	private void newBricks() {
		int brickWidth = getWidth() / 10;
		int brickHeight = brickWidth / 4;
		int brickSpacing = brickHeight / 2;
		int hSpace = brickWidth + brickSpacing;
		int vSpace = brickHeight + brickSpacing;
		Brick.Type[] brickTypes = Brick.Type.values();

		cols = (getWidth() - brickSpacing) / hSpace - 2;
		rows = brickTypes.length;
		int startX = (getWidth() - cols * hSpace + brickSpacing) / 2;
		int startY = brickHeight * 3;

		int x = startX, y = startY;
		for (int row = 0; row < rows; ++row) {
			Brick.Type type = brickTypes[row];
			int value = 5 * (brickTypes.length - row);
			for (int col = 0; col < cols; ++col) {
				Brick brick = new Brick(brickWidth, brickHeight, type, value);
				brick.tf.moveTo(x, y);
				app.entities.add(brick);
				app.collisionHandler.registerStart(ball, brick, BallHitsBrick);
				x += hSpace;
			}
			x = startX;
			y += vSpace;
		}
	}

	private void removeBrick(Brick brick) {
		app.entities.remove(brick);
		app.collisionHandler.unregisterStart(ball, brick);
	}

	private void reset() {
		points = 0;
		resetBatAndBall();
		newBricks();
	}

	private void resetBatAndBall() {
		bat.speed = getWidth() / 48;
		bat.tf.moveTo((getWidth() - bat.getWidth()) / 2, getHeight() - bat.getHeight());
		bat.tf.setVelocity(0, 0);
		ball.tf.moveTo((getWidth() - ball.getWidth()) / 2, bat.tf.getY() - ball.getHeight());
		ball.tf.setVelocity(0, 0);
	}

	private void bounceBallFromBat() {
		ball.tf.setVelocityY(-ball.tf.getVelocityY());
		Assets.sound("Sounds/plop.mp3").play();
	}

	private void bounceBallFromBrick(Brick brick) {
		ball.tf.setVelocityY(-ball.tf.getVelocityY());
	}

	private void shootBall() {
		resetBatAndBall();
		Random random = new Random();
		ball.tf.setVelocityX(2 + random.nextFloat() * 2);
		ball.tf.setVelocityY(-(6 + random.nextFloat() * 6));
		if (random.nextBoolean()) {
			ball.tf.setVelocityX(-ball.tf.getVelocityX());
		}
	}

	private void onBrickHit(PlayEvent e) {
		Brick brick = e.getUserData();
		if (brick.isDamaged()) {
			removeBrick(brick);
			points += brick.getValue();
			Assets.sound("Sounds/point.mp3").play();
		} else {
			brick.damage();
			bounceBallFromBrick(brick);
		}
	}
}