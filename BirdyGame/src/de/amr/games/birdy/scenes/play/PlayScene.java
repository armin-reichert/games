package de.amr.games.birdy.scenes.play;

import static de.amr.easy.game.entity.collision.CollisionHandler.detectCollisionEnd;
import static de.amr.easy.game.entity.collision.CollisionHandler.detectCollisionStart;
import static de.amr.easy.game.entity.collision.CollisionHandler.ignoreCollisionEnd;
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
import de.amr.games.birdy.entities.PipePair;
import de.amr.games.birdy.entities.ScoreDisplay;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.start.StartScene;

/**
 * Play scene of the game.
 * 
 * @author Armin Reichert
 */
public class PlayScene extends Scene<BirdyGame> {

	private final Score score = new Score();

	private class PlaySceneControl extends StateMachine<PlaySceneState, BirdyGameEvent> {

		public PlaySceneControl() {
			super("Play Scene Control", PlaySceneState.class, Playing);

			state(Playing).entry = s -> {
				score.reset();
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

	// Pipe creation/deletion/update is encapsulated in the following inner class:

	/** Pipe manager states. */
	public enum PipeManagerState {
		Stopped, Breeding, Birthing
	};

	/**
	 * Manages the creation and deletion of pipes.
	 */
	private class PipeManager extends GameEntity {

		private final List<PipePair> pairs = new LinkedList<>();
		private final StateMachine<PipeManagerState, String> control;

		private PipeManager() {
			control = new StateMachine<>("PipeManagerControl", PipeManagerState.class, PipeManagerState.Stopped);

			// Stay breeding for some random time from interval [MIN_PIPE_TIME, MAX_PIPE_TIME]:
			control.state(PipeManagerState.Breeding).entry = s -> {
				int minCreationTime = app.motor.secToTicks(app.settings.getFloat("min pipe creation sec"));
				int maxCreationTime = app.motor.secToTicks(app.settings.getFloat("max pipe creation sec"));
				s.setDuration(randomInt(minCreationTime, maxCreationTime));
			};

			// Update (move) pipes during breeding:
			control.state(PipeManagerState.Breeding).update = s -> {
				pairs.forEach(PipePair::update);
			};

			// When breeding is over, give birth to new pipe pair:
			control.changeOnTimeout(PipeManagerState.Breeding, PipeManagerState.Birthing, (s, t) -> {
				addPipePair();
				removeObsoletePipePairs();
			});

			// On "Stop" event, enter "Stopped" state:
			control.changeOnInput("Stop", PipeManagerState.Breeding, PipeManagerState.Stopped);

			// Immediately become breeding again:
			control.change(PipeManagerState.Birthing, PipeManagerState.Breeding);

			// Handle "Stop" event:
			control.changeOnInput("Stop", PipeManagerState.Birthing, PipeManagerState.Stopped);

			// On "Start" event, become breeding again:
			control.changeOnInput("Start", PipeManagerState.Stopped, PipeManagerState.Breeding);
		}

		@Override
		public void init() {
			control.init();
		}

		public void start() {
			control.addInput("Start");
		}

		public void stop() {
			control.addInput("Stop");
			control.update();
		}

		@Override
		public void update() {
			control.update();
		}

		@Override
		public void draw(Graphics2D g) {
			pairs.forEach(pair -> pair.render(g));
		}

		private void addPipePair() {
			int minPipeHeight = app.settings.get("min pipe height");
			int passageHeight = app.settings.get("passage height");
			int centerY = randomInt(minPipeHeight + passageHeight / 2,
					(int) ground.tr.getY() - minPipeHeight - passageHeight / 2);
			PipePair pair = new PipePair(app, centerY);
			pair.setPositionX(getApp().getWidth());
			pair.setLighted(city.isNight() && new Random().nextBoolean());
			pairs.add(pair);
			detectCollisionStart(bird, pair.getPipeDown(), BirdTouchedPipe);
			detectCollisionStart(bird, pair.getPipeUp(), BirdTouchedPipe);
			detectCollisionEnd(bird, pair.getPassage(), BirdLeftPassage);
		}

		private void removeObsoletePipePairs() {
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
		scoreDisplay = new ScoreDisplay(app.assets, score, 1.5f);
		scoreDisplay.centerHor(getWidth());
		scoreDisplay.tr.setY(ground.tr.getY() / 4);
		gameOverText = app.entities.add(new GameOverText(app.assets));
		gameOverText.center(getWidth(), getHeight());
		pipesManager = new PipeManager();
		CollisionHandler.detectCollisionStart(bird, ground, BirdTouchedGround);
		Area world = new Area(0, -getHeight(), getWidth(), 2 * getHeight());
		CollisionHandler.detectCollisionEnd(bird, world, BirdLeftWorld);

		pipesManager.init();
		control.init();

		// Tracing
		// pipesManager.control.setLogger(Application.LOG);
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
		pipesManager.draw(g);
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
		pipesManager.start();
	}

	private void stop() {
		ground.tr.setVelocity(0, 0);
		pipesManager.stop();
	}

	private Font stateTextFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

	private void showState(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setFont(stateTextFont);
		g.drawString(format("%s: %s  Bird: %s & %s", control.getDescription(), control.stateID(), bird.getFlightState(),
				bird.getHealthState()), 20, getHeight() - 50);
	}
}