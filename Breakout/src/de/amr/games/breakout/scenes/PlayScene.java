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

/**
 * The playing scene.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class PlayScene extends Scene<BreakoutGame> {

	private final PlaySceneControl control;
	private Bat bat;
	private Ball ball;
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
			state(Initialized).entry = s -> {
				points = 0;
				resetBatAndBall();
				newBricks();
			};

			change(Initialized, Playing, () -> keyPressedOnce(VK_SPACE));

			// Playing
			state(Playing).entry = s -> launchBall();

			state(Playing).update = s -> {
				if (app.entities.filter(Brick.class).count() == 0) {
					resetBatAndBall();
					newBricks();
				}
				app.entities.all().forEach(GameEntity::update);
			};

			changeOnInput(BallHitsBat, Playing, Playing, t -> {
				ball.tf.setVelocityY(-ball.tf.getVelocityY());
				Assets.sound("Sounds/plop.mp3").play();
			});

			changeOnInput(BallHitsBrick, Playing, Playing, t -> {
				Brick brick = t.getInput().get().getUserData();
				if (brick.isDamaged()) {
					app.entities.remove(brick);
					app.collisionHandler.unregisterStart(ball, brick);
					points += brick.getValue();
					Assets.sound("Sounds/point.mp3").play();
				} else {
					brick.damage();
					ball.tf.setVelocityY(-ball.tf.getVelocityY());
				}
			});

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

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		drawScore(g);
	}

	private void newBricks() {
		final Brick.Type[] brickTypes = Brick.Type.values();
		final int brickWidth = getWidth() / 10;
		final int brickHeight = brickWidth / 4;
		final int brickSpacing = brickHeight / 2;
		final int hSpace = brickWidth + brickSpacing;
		final int vSpace = brickHeight + brickSpacing;
		final int numRows = brickTypes.length;
		final int numCols = (getWidth() - brickSpacing) / hSpace - 2;
		final int startX = (getWidth() - numCols * hSpace + brickSpacing) / 2;
		final int startY = brickHeight * 3;

		int x = startX, y = startY;
		for (int row = 0; row < numRows; ++row) {
			Brick.Type type = brickTypes[row];
			int value = 5 * (numRows - row);
			for (int col = 0; col < numCols; ++col) {
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

	private void resetBatAndBall() {
		bat.speed = getWidth() / 48;
		bat.tf.moveTo((getWidth() - bat.getWidth()) / 2, getHeight() - bat.getHeight());
		bat.tf.setVelocity(0, 0);
		ball.tf.moveTo((getWidth() - ball.getWidth()) / 2, bat.tf.getY() - ball.getHeight());
		ball.tf.setVelocity(0, 0);
	}

	private void launchBall() {
		Random random = new Random();
		float vx = 2 + random.nextFloat() * 2;
		if (random.nextBoolean()) {
			vx = -vx;
		}
		float vy = -(6 + random.nextFloat() * 6);
		ball.tf.setVelocity(vx, vy);
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

	private final Font scoreFont = new Font(Font.SANS_SERIF, Font.PLAIN, 48);

	private void drawScore(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.RED);
		g.setFont(scoreFont);
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(String.valueOf(points), g);
		g.drawString(String.valueOf(points), (int) (getWidth() - bounds.getWidth()) / 2, getHeight() * 3 / 4);
	}
}