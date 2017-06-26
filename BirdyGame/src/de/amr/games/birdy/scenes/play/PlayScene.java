package de.amr.games.birdy.scenes.play;

import static de.amr.easy.game.entity.collision.CollisionHandler.detectCollisionEnd;
import static de.amr.easy.game.entity.collision.CollisionHandler.detectCollisionStart;
import static de.amr.easy.game.entity.collision.CollisionHandler.ignoreCollisionEnd;
import static de.amr.games.birdy.BirdyGame.MAX_PIPE_TIME;
import static de.amr.games.birdy.BirdyGame.MIN_PIPE_TIME;
import static de.amr.games.birdy.BirdyGame.OBSTACLE_MIN_PIPE_HEIGHT;
import static de.amr.games.birdy.BirdyGame.OBSTACLE_PASSAGE_HEIGHT;
import static de.amr.games.birdy.BirdyGame.WORLD_SPEED;
import static de.amr.games.birdy.BirdyGameEvent.BirdCrashed;
import static de.amr.games.birdy.BirdyGameEvent.BirdLeftPassage;
import static de.amr.games.birdy.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.scenes.play.PlaySceneState.GameOver;
import static de.amr.games.birdy.scenes.play.PlaySceneState.Playing;
import static de.amr.games.birdy.scenes.play.PlaySceneState.StartingNewGame;
import static de.amr.games.birdy.utils.Util.randomInt;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import de.amr.easy.game.Application;
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
import de.amr.games.birdy.entities.PipeDown;
import de.amr.games.birdy.entities.PipePair;
import de.amr.games.birdy.entities.PipeUp;
import de.amr.games.birdy.entities.ScoreDisplay;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.start.StartScene;

/**
 * Play scene of the game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<BirdyGame> {

	private class PlaySceneControl extends StateMachine<PlaySceneState, BirdyGameEvent> {

		public PlaySceneControl() {
			super("Play Scene Control", PlaySceneState.class, Playing);

			state(Playing).entry = s -> {
				app.score.reset();
				startScrolling();
				app.SND_PLAYING.loop();
			};

			changeOnInput(BirdTouchedPipe, Playing, Playing, () -> app.score.points > 3, (s, t) -> {
				app.SND_BIRD_HITS_OBSTACLE.play();
				app.score.points -= 3;
				bird.tr.setX(bird.tr.getX() + BirdyGame.OBSTACLE_PIPE_WIDTH + bird.getWidth());
				bird.receiveEvent(BirdTouchedPipe);
			});

			changeOnInput(BirdTouchedPipe, Playing, GameOver, () -> app.score.points <= 3, (s, t) -> {
				app.SND_BIRD_HITS_OBSTACLE.play();
				bird.receiveEvent(BirdCrashed);
			});

			changeOnInput(BirdLeftPassage, Playing, Playing, (s, t) -> {
				app.SND_WON_POINT.play();
				app.score.points++;
				Application.LOG.info("Score: " + app.score.points);
			});

			changeOnInput(BirdTouchedGround, Playing, GameOver, (s, t) -> {
				app.SND_PLAYING.stop();
				bird.receiveEvent(BirdTouchedGround);
			});

			changeOnInput(BirdLeftWorld, Playing, GameOver, (s, t) -> {
				app.SND_PLAYING.stop();
				bird.receiveEvent(BirdLeftWorld);
			});

			state(GameOver).entry = s -> {
				stopScrolling();
				pipesManager.stop();
			};

			change(GameOver, StartingNewGame, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
			changeOnInput(BirdTouchedGround, GameOver, GameOver, (s, t) -> app.SND_PLAYING.stop());

			state(StartingNewGame).entry = s -> app.views.show(StartScene.class);
		}
	}

	private final PlaySceneControl control;
	private PipeManager pipesManager;
	private Bird bird;
	private City city;
	private Ground ground;
	private GameEntity gameOverText;
	private ScoreDisplay scoreDisplay;

	public PlayScene(BirdyGame game) {
		super(game);
		control = new PlaySceneControl();
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
		scoreDisplay = new ScoreDisplay(app.assets, app.score, 1.5f);
		scoreDisplay.centerHor(getWidth());
		scoreDisplay.tr.setY(ground.tr.getY() / 4);
		gameOverText = app.entities.add(new GameOverText(app.assets));
		gameOverText.center(getWidth(), getHeight());
		pipesManager = new PipeManager();
		CollisionHandler.detectCollisionStart(bird, ground, BirdTouchedGround);
		Area birdWorld = new Area(0, -getHeight(), getWidth(), 2 * getHeight());
		CollisionHandler.detectCollisionEnd(bird, birdWorld, BirdLeftWorld);
		control.init();
	}

	@Override
	public void update() {
		bird.update();
		city.update();
		ground.update();
		pipesManager.update();
		gameOverText.update();
		scoreDisplay.update();
		for (Collision collision : CollisionHandler.collisions()) {
			receive((BirdyGameEvent) collision.getAppEvent());
		}
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		city.draw(g);
		pipesManager.render(g);
		ground.draw(g);
		scoreDisplay.draw(g);
		bird.draw(g);
		if (control.stateID() == PlaySceneState.GameOver) {
			gameOverText.draw(g);
		}
		showState(g);
	}

	private void showState(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		g.drawString(format("%s: %s  Bird: %s & %s", control.getDescription(), control.stateID(), bird.getFlightState(),
				bird.getHealthState()), 20, getHeight() - 50);
	}

	@Override
	public String toString() {
		return control.getDescription() + " (" + control.stateID() + ")";
	}

	void startScrolling() {
		ground.tr.setVel(WORLD_SPEED, 0);
		pipesManager.start();
	}

	void stopScrolling() {
		ground.tr.setVel(0, 0);
		pipesManager.stop();
	}

	// --- inner class for managing creation/deletion of pipe pairs

	private class PipeManager {

		private final List<PipePair> pairs = new LinkedList<>();
		private final StateMachine<String, String> control;

		private PipeManager() {
			control = new StateMachine<>("PipeManagerControl", String.class, "Stopped");

			// Stay breeding for some random time from interval:
			control.state("Breeding").entry = s -> s.setDuration(randomInt(MIN_PIPE_TIME, MAX_PIPE_TIME));
			// Update (move) pipes during breeding:
			control.state("Breeding").update = s -> pairs.forEach(PipePair::update);

			// When breeding is over, give birth to new pipe pair:
			control.changeOnTimeout("Breeding", "Birthing", (s, t) -> addPair());
			// On "Stop" event, enter "Stopped" state:
			control.changeOnInput("Stop", "Breeding", "Stopped");

			// Immediately become breeding again:
			control.change("Birthing", "Breeding");
			// Handle "Stop" event:
			control.changeOnInput("Stop", "Birthing", "Stopped");

			// On "Start" event, become breeding again:
			control.changeOnInput("Start", "Stopped", "Breeding");

			// Tracing
			control.setLogger(Application.LOG);
		}

		public void start() {
			control.init();
			control.addInput("Start");
		}

		public void stop() {
			control.addInput("Stop");
		}

		public void update() {
			control.update();
			removeObsoletePipes();
		}

		public void render(Graphics2D g) {
			pairs.forEach(pair -> pair.render(g));
		}

		private void addPair() {
			PipePair pair = new PipePair(app, randomInt(OBSTACLE_MIN_PIPE_HEIGHT + OBSTACLE_PASSAGE_HEIGHT / 2,
					(int) ground.tr.getY() - OBSTACLE_MIN_PIPE_HEIGHT - OBSTACLE_PASSAGE_HEIGHT / 2));
			pair.setPositionX(getApp().getWidth());
			pair.setLighted(city.isNight() && new Random().nextBoolean());
			pairs.add(pair);
			detectCollisionStart(bird, pair.getPipeDown(), BirdTouchedPipe);
			detectCollisionStart(bird, pair.getPipeUp(), BirdTouchedPipe);
			detectCollisionEnd(bird, pair.getPassage(), BirdLeftPassage);
		}

		private void removeObsoletePipes() {
			Iterator<PipePair> it = pairs.iterator();
			while (it.hasNext()) {
				PipePair pipes = it.next();
				if (pipes.getPositionX() + pipes.getWidth() < 0) {
					ignoreCollisionEnd(bird, pipes.getPipeDown());
					ignoreCollisionEnd(bird, pipes.getPipeUp());
					ignoreCollisionEnd(bird, pipes.getPassage());
					it.remove();
				}
			}
			app.entities.removeAll(PipeUp.class);
			app.entities.removeAll(PipeDown.class);
		}
	}
}