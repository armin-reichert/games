package de.amr.games.birdy.play.scenes;

import static de.amr.games.birdy.play.BirdyGameEvent.BirdCrashed;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdLeftPassage;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.play.scenes.PlayScene.State.GameOver;
import static de.amr.games.birdy.play.scenes.PlayScene.State.Playing;
import static de.amr.games.birdy.play.scenes.PlayScene.State.StartingNewGame;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.common.Score;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.entities.Area;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.ObstacleManager;
import de.amr.games.birdy.entities.ScoreDisplay;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.play.BirdyGame;
import de.amr.games.birdy.play.BirdyGameEvent;

/**
 * Play scene of the game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<BirdyGame> {

	private final PlaySceneControl control;
	private final Score score = new Score();
	private final ObstacleManager obstacleManager;
	private Bird bird;
	private City city;
	private Ground ground;
	private GameEntity gameOverText;
	private ScoreDisplay scoreDisplay;

	public enum State {
		Playing, GameOver, StartingNewGame;
	}

	private class PlaySceneControl extends StateMachine<State, BirdyGameEvent> {

		public PlaySceneControl() {
			super("Play Scene Control", State.class, Playing);

			state(Playing).entry = s -> {
				score.reset();
				obstacleManager.init();
				start();
			};

			changeOnInput(BirdTouchedPipe, Playing, Playing, () -> score.points > 3, (s, t) -> {
				score.points -= 3;
				bird.tf.setX(bird.tf.getX() + app.settings.getAsInt("pipe width") + bird.getWidth());
				bird.receiveEvent(BirdTouchedPipe);
				app.assets.sound("sfx/hit.mp3").play();
			});

			changeOnInput(BirdTouchedPipe, Playing, GameOver, () -> score.points <= 3, (s, t) -> {
				bird.receiveEvent(BirdCrashed);
				app.assets.sound("sfx/hit.mp3").play();
			});

			changeOnInput(BirdLeftPassage, Playing, Playing, (s, t) -> {
				score.points++;
				app.assets.sound("sfx/point.mp3").play();
			});

			changeOnInput(BirdTouchedGround, Playing, GameOver, (s, t) -> {
				bird.receiveEvent(BirdTouchedGround);
				app.assets.sound("music/bgmusic.mp3").stop();
			});

			changeOnInput(BirdLeftWorld, Playing, GameOver, (s, t) -> {
				bird.receiveEvent(BirdLeftWorld);
				app.assets.sound("music/bgmusic.mp3").stop();
			});

			state(GameOver).entry = s -> stop();

			change(GameOver, StartingNewGame, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
			changeOnInput(BirdTouchedGround, GameOver, GameOver, (s, t) -> app.assets.sound("music/bgmusic.mp3").stop());

			state(StartingNewGame).entry = s -> app.selectView(StartScene.class);
		}
	}

	public PlayScene(BirdyGame game) {
		super(game);
		control = new PlaySceneControl();
		obstacleManager = new ObstacleManager(app);
		// control.setLogger(Application.LOG);
	}

	public void receive(BirdyGameEvent event) {
		control.addInput(event);
		bird.receiveEvent(event);
	}

	@Override
	public void init() {
		ground = app.entities.findAny(Ground.class);
		city = app.entities.findAny(City.class);
		bird = app.entities.findAny(Bird.class);
		scoreDisplay = new ScoreDisplay(app.assets, score, 1.5f);
		scoreDisplay.hCenter(getWidth());
		scoreDisplay.tf.setY(ground.tf.getY() / 4);
		gameOverText = app.entities.add(new GameEntity(new Sprite(app.assets.image("text_game_over"))));
		gameOverText.center(getWidth(), getHeight());
		Area world = new Area(getWidth(), 2 * getHeight());
		world.tf.moveTo(0, -getHeight());

		app.collisionHandler.registerStart(bird, ground, BirdTouchedGround);
		app.collisionHandler.registerEnd(bird, world, BirdLeftWorld);

		obstacleManager.init();
		// obstacleManager.setLogger(Application.LOG);
		control.init();
	}

	@Override
	public void update() {
		for (Collision collision : app.collisionHandler.collisions()) {
			receive((BirdyGameEvent) collision.getAppEvent());
		}
		control.update();
		obstacleManager.update();
		bird.update();
		city.update();
		ground.update();
		gameOverText.update();
		scoreDisplay.update();
	}

	@Override
	public void draw(Graphics2D g) {
		city.draw(g);
		obstacleManager.draw(g);
		ground.draw(g);
		scoreDisplay.draw(g);
		bird.draw(g);
		if (control.stateID() == GameOver) {
			gameOverText.draw(g);
		}
		showState(g);
	}

	private void start() {
		ground.tf.setVelocity(app.settings.get("world speed"), 0);
		obstacleManager.start();
	}

	private void stop() {
		ground.tf.setVelocity(0, 0);
		obstacleManager.stop();
	}

	private Font stateTextFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

	private void showState(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setFont(stateTextFont);
		g.drawString(format("%s: %s  Bird: %s & %s", control.getDescription(), control.stateID(), bird.getFlightState(),
				bird.getHealthState()), 20, getHeight() - 50);
	}
}