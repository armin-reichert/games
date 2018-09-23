package de.amr.games.breakout.controller;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.breakout.controller.PlayState.BallOut;
import static de.amr.games.breakout.controller.PlayState.Initialized;
import static de.amr.games.breakout.controller.PlayState.Playing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.AbstractGameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.games.breakout.BreakoutGameApp;
import de.amr.games.breakout.entities.Ball;
import de.amr.games.breakout.entities.Bat;
import de.amr.games.breakout.entities.Brick;
import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

/**
 * The playing scene.
 * 
 * @author Armin Reichert & Anna Schillo
 */
public class PlayScene implements View, Controller {

	private final BreakoutGameApp app;
	private final StateMachine<PlayState, PlayEvent> control;
	private Bat bat;
	private Ball ball;
	private int points;
	private Image bgImage;

	private final Font scoreFont = new Font(Font.SANS_SERIF, Font.PLAIN, 48);

	public PlayScene(BreakoutGameApp app) {
		this.app = app;
		bgImage = Assets.image("background.jpg").getScaledInstance(getWidth(), getHeight(),
				BufferedImage.SCALE_SMOOTH);
		Dimension boardSize = new Dimension(getWidth(), getHeight());

		ball = new Ball(app.settings.get("ball_size"));
		ball.setBoardSize(boardSize);
		app.entities.store(ball);

		bat = new Bat(app.settings.get("bat_width"), app.settings.get("bat_height"));
		bat.setBoardSize(boardSize);
		app.entities.store(bat);

		app.collisionHandler.registerStart(ball, bat, new BallHitsBatEvent());
		control = buildStateMachine();
		control.traceTo(LOGGER, app().clock::getFrequency);
	}

	public int getWidth() {
		return app.settings.width;
	}

	public int getHeight() {
		return app.settings.height;
	}

	private StateMachine<PlayState, PlayEvent> buildStateMachine() {
		//@formatter:off
		return StateMachine.beginStateMachine(PlayState.class, PlayEvent.class, Match.BY_CLASS)
			
			.description("BreakoutGameControl")
			.initialState(Initialized)

			.states()
			
				.state(Initialized).onEntry( () -> {
					points = 0;
					resetBatAndBall();
					newBricks();
				})
				
				.state(Playing)
					.onEntry(this::launchBall)
					.onTick( () -> {
						if (app.entities.ofClass(Brick.class).count() == 0) {
							resetBatAndBall();
							newBricks();
							launchBall();
						}
						app.entities.all().forEach(AbstractGameEntity::update);
					})
					
				.state(BallOut)
					.onEntry(this::resetBatAndBall)

			.transitions()
			
				.when(Initialized).then(Playing).condition( () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE) )

				.stay(Playing).on(BallHitsBatEvent.class).act(e -> {
					ball.tf.setVelocityY(-ball.tf.getVelocityY());
					Assets.sound("Sounds/plop.mp3").play();
				})
				
				.stay(Playing).on(BallHitsBrickEvent.class).act(e -> {
					Brick brick = ((BallHitsBrickEvent) e).brick;
					if (brick.isDamaged()) {
						app.entities.removeEntity(brick);
						app.collisionHandler.unregisterStart(ball, brick);
						points += brick.getValue();
						Assets.sound("Sounds/point.mp3").play();
					} else {
						brick.damage();
						ball.tf.setVelocityY(-ball.tf.getVelocityY());
					}
				})

			  .when(Playing).then(BallOut).condition(ball::isOut)

			  .when(BallOut).then(Playing).condition( () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE))
			
			.endStateMachine();
			//@formatter:on
	}

	@Override
	public void init() {
		control.init();
	}

	@Override
	public void update() {
		handleCollisions();
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		if (bgImage != null) {
			g.drawImage(bgImage, 0, 0, null);
		}
		app.entities.all().filter(entity -> entity instanceof View).forEach(entity -> {
			((View) entity).draw(g);
		});
		drawScore(g);
	}

	private void newBricks() {
		final Brick.BrickColor[] brickTypes = Brick.BrickColor.values();
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
			Brick.BrickColor type = brickTypes[row];
			int value = 5 * (numRows - row);
			for (int col = 0; col < numCols; ++col) {
				Brick brick = new Brick(brickWidth, brickHeight, type, value);
				brick.tf.setPosition(x, y);
				app.entities.store(brick);
				app.collisionHandler.registerStart(ball, brick, new BallHitsBrickEvent(brick));
				x += hSpace;
			}
			x = startX;
			y += vSpace;
		}
	}

	private void resetBatAndBall() {
		bat.speed = getWidth() / 48;
		bat.tf.setPosition((getWidth() - bat.tf.getWidth()) / 2, getHeight() - bat.tf.getHeight());
		bat.tf.setVelocity(0, 0);
		ball.tf.setPosition((getWidth() - ball.tf.getWidth()) / 2, bat.tf.getY() - ball.tf.getHeight());
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
			control.enqueue(event);
		}
	}

	private void drawScore(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setColor(Color.RED);
		g.setFont(scoreFont);
		Rectangle2D bounds = g.getFontMetrics().getStringBounds(String.valueOf(points), g);
		g.drawString(String.valueOf(points), (int) (getWidth() - bounds.getWidth()) / 2, getHeight() * 3 / 4);
	}
}