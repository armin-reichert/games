package de.amr.games.birdy.scenes.play;

import static de.amr.games.birdy.BirdyGameEvent.BirdCrashed;
import static de.amr.games.birdy.BirdyGameEvent.BirdLeftPassage;
import static de.amr.games.birdy.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.scenes.play.PlaySceneState.GameOver;
import static de.amr.games.birdy.scenes.play.PlaySceneState.Playing;
import static de.amr.games.birdy.scenes.play.PlaySceneState.StartingNewGame;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.entity.collision.CollisionHandler;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.BirdyGameEvent;
import de.amr.games.birdy.entities.Area;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.GameOverText;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.ObstacleManager;
import de.amr.games.birdy.entities.ScoreDisplay;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.start.StartScene;

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

	private class PlaySceneControl extends StateMachine<PlaySceneState, BirdyGameEvent> {

		public PlaySceneControl() {
			super("Play Scene Control", PlaySceneState.class, Playing);

			state(Playing).entry = s -> {
				score.reset();
				obstacleManager.init();
				start();
			};

			changeOnInput(BirdTouchedPipe, Playing, Playing, () -> score.points > 3, (s, t) -> {
				score.points -= 3;
				bird.tr.setX(bird.tr.getX() + app.settings.getInt("pipe width") + bird.getWidth());
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

			state(StartingNewGame).entry = s -> app.views.show(StartScene.class);
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
		scoreDisplay.centerHor(getWidth());
		scoreDisplay.tr.setY(ground.tr.getY() / 4);
		gameOverText = app.entities.add(new GameOverText(app.assets));
		gameOverText.center(getWidth(), getHeight());
		CollisionHandler.detectCollisionStart(bird, ground, BirdTouchedGround);
		Area world = new Area(0, -getHeight(), getWidth(), 2 * getHeight());
		CollisionHandler.detectCollisionEnd(bird, world, BirdLeftWorld);

		obstacleManager.init();
		control.init();
		// Tracing
		obstacleManager.setLogger(Application.LOG);
	}

	@Override
	public void update() {
		for (Collision collision : CollisionHandler.collisions()) {
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
		ground.tr.setVelocity(app.settings.get("world speed"), 0);
		obstacleManager.start();
	}

	private void stop() {
		ground.tr.setVelocity(0, 0);
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