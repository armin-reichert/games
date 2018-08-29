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

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.controls.Score;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.entities.Area;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.GraphicText;
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
public class PlayScene implements View,Controller {

	private final BirdyGame app;
	private final PlaySceneControl control;
	private final Score score = new Score();
	private final ObstacleManager obstacleManager;
	private Bird bird;
	private City city;
	private Ground ground;
	private GraphicText gameOverText;
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

			changeOnInput(BirdTouchedPipe, Playing, Playing, () -> score.points > 3, t -> {
				score.points -= 3;
				bird.tf()
						.setX(bird.tf().getX() + app.settings.getAsInt("pipe width") + bird.tf().getWidth());
				bird.receiveEvent(BirdTouchedPipe);
				Assets.sound("sfx/hit.mp3").play();
			});

			changeOnInput(BirdTouchedPipe, Playing, GameOver, () -> score.points <= 3, t -> {
				bird.receiveEvent(BirdCrashed);
				Assets.sound("sfx/hit.mp3").play();
			});

			changeOnInput(BirdLeftPassage, Playing, Playing, t -> {
				score.points++;
				Assets.sound("sfx/point.mp3").play();
			});

			changeOnInput(BirdTouchedGround, Playing, GameOver, t -> {
				bird.receiveEvent(BirdTouchedGround);
				Assets.sound("music/bgmusic.mp3").stop();
			});

			changeOnInput(BirdLeftWorld, Playing, GameOver, t -> {
				bird.receiveEvent(BirdLeftWorld);
				Assets.sound("music/bgmusic.mp3").stop();
			});

			state(GameOver).entry = s -> stop();

			change(GameOver, StartingNewGame, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
			changeOnInput(BirdTouchedGround, GameOver, GameOver,
					t -> Assets.sound("music/bgmusic.mp3").stop());

			state(StartingNewGame).entry = s -> app.setController(app.getStartScene());
		}
	}

	public PlayScene(BirdyGame game) {
		this.app = game;
		control = new PlaySceneControl();
		obstacleManager = new ObstacleManager(app);
	}

	public void receive(BirdyGameEvent event) {
		control.addInput(event);
		bird.receiveEvent(event);
	}

	public int getWidth() {
		return app.settings.width;
	}

	public int getHeight() {
		return app.settings.height;
	}

	@Override
	public void init() {
		ground = app.entities.ofClass(Ground.class).findAny().get();
		city = app.entities.ofClass(City.class).findAny().get();
		bird = app.entities.ofClass(Bird.class).findAny().get();
		scoreDisplay = new ScoreDisplay(score, 1.5f);
		scoreDisplay.centerHorizontally(getWidth());
		scoreDisplay.tf().setY(ground.tf().getY() / 4);
		gameOverText = app.entities.store(new GraphicText(Assets.image("text_game_over")));
		gameOverText.center(getWidth(), getHeight());
		Area world = new Area(getWidth(), 2 * getHeight());
		world.tf().moveTo(0, -getHeight());

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
		if (control.is(GameOver)) {
			gameOverText.draw(g);
		}
		showState(g);
	}

	private void start() {
		ground.tf().setVelocity(app.settings.get("world speed"), 0);
		obstacleManager.start();
	}

	private void stop() {
		ground.tf().setVelocity(0, 0);
		obstacleManager.stop();
	}

	private Font stateTextFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

	private void showState(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setFont(stateTextFont);
		g.drawString(format("%s: %s  Bird: %s & %s", control.getDescription(), control.stateID(),
				bird.getFlightState(), bird.getHealthState()), 20, getHeight() - 50);
	}
}