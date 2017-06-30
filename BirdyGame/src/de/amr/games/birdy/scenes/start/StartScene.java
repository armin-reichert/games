package de.amr.games.birdy.scenes.start;

import static de.amr.games.birdy.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.scenes.start.StartSceneState.GameOver;
import static de.amr.games.birdy.scenes.start.StartSceneState.Ready;
import static de.amr.games.birdy.scenes.start.StartSceneState.StartPlaying;
import static de.amr.games.birdy.scenes.start.StartSceneState.Starting;
import static de.amr.games.birdy.utils.Util.randomInt;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.common.PumpingImage;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.scene.Scene;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.BirdyGameEvent;
import de.amr.games.birdy.entities.Area;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.play.PlayScene;

/**
 * Start scene of the game: bird flaps in the air until user presses the JUMP key.
 * 
 * @author Armin Reichert
 */
public class StartScene extends Scene<BirdyGame> {

	private class StartSceneControl extends StateMachine<StartSceneState, BirdyGameEvent> {

		public StartSceneControl() {
			super("Start Scene Control", StartSceneState.class, Starting);

			// Starting ---

			state(Starting).entry = s -> {
				reset();
				if (!app.assets.sound("music/bgmusic.mp3").isRunning()) {
					app.assets.sound("music/bgmusic.mp3").loop();
				}
			};

			state(Starting).update = s -> keepBirdInAir();

			change(Starting, Ready, () -> Keyboard.keyDown(app.settings.get("jump key")));

			changeOnInput(BirdTouchedGround, Starting, GameOver);

			// Ready ---

			state(Ready).entry = s -> {
				s.setDuration(app.motor.secToTicks(app.settings.getFloat("ready time sec")));
				displayText("readyText");
			};

			changeOnTimeout(Ready, StartPlaying, (s, t) -> app.views.show(PlayScene.class));

			changeOnInput(BirdTouchedGround, Ready, GameOver, (s, t) -> displayText("title"));

			state(Ready).exit = s -> displayedText = Optional.empty();

			// GameOver ---

			state(GameOver).entry = s -> {
				stop();
				displayedText = Optional.of(app.entities.findByName(GameEntity.class, "game_over"));
				app.assets.sounds().forEach(Sound::stop);
			};

			change(GameOver, Starting, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}
	}

	private final StartSceneControl control = new StartSceneControl();

	private Bird bird;
	private City city;
	private Ground ground;
	private Optional<GameEntity> displayedText;

	public StartScene(BirdyGame game) {
		super(game);
	}

	@Override
	public void init() {
		// control.setLogger(Application.LOG);
		control.init();
	}

	private void stop() {
		ground.tr.setVelocity(0, 0);
	}

	private void keepBirdInAir() {
		while (bird.tr.getY() > ground.tr.getY() / 2) {
			bird.flap(randomInt(1, 4));
		}
	}

	private void displayText(String name) {
		displayedText = Optional.of(app.entities.findByName(GameEntity.class, name));
	}

	private void reset() {
		city = app.entities.findAny(City.class);
		city.setWidth(getWidth());

		ground = app.entities.findAny(Ground.class);
		ground.setWidth(getWidth());
		ground.tr.moveTo(0, getHeight() - ground.getHeight());
		ground.tr.setVelocity(app.settings.getFloat("world speed"), 0);

		bird = app.entities.findAny(Bird.class);
		bird.init();
		bird.tr.moveTo(getWidth() / 8, ground.tr.getY() / 2);
		bird.tr.setVelocity(0, 0);
		bird.setFeathers(city.isNight() ? bird.BLUE_FEATHERS : bird.YELLOW_FEATHERS);

		if (!app.entities.contains("title")) {
			GameEntity titleText = new GameEntity(new Sprite(app.assets.image("title")));
			titleText.setName("title");
			app.entities.add(titleText);
		}

		if (!app.entities.contains("text_game_over")) {
			GameEntity gameOverText = new GameEntity(new Sprite(app.assets.image("text_game_over")));
			gameOverText.setName("game_over");
			app.entities.add(gameOverText);
		}

		if (!app.entities.contains("text_ready")) {
			PumpingImage readyText = new PumpingImage(app.assets.image("text_ready"));
			readyText.setName("readyText");
			app.entities.add(readyText);
		}

		if (!app.entities.contains("world")) {
			Area world = new Area(getWidth(), 2 * getHeight());
			world.setName("world");
			world.tr.moveTo(0, -getHeight());
			app.entities.add(world);
		}

		app.collisionHandler.clear();
		app.collisionHandler.registerEnd(bird, app.entities.findAny(Area.class), BirdLeftWorld);
		app.collisionHandler.registerStart(bird, ground, BirdTouchedGround);

		displayText("title");
	}

	@Override
	public void update() {
		for (Collision c : app.collisionHandler.collisions()) {
			BirdyGameEvent event = (BirdyGameEvent) c.getAppEvent();
			bird.receiveEvent(event);
			control.addInput(event);
		}
		city.update();
		ground.update();
		bird.update();
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		city.draw(g);
		ground.draw(g);
		bird.draw(g);
		displayedText.ifPresent(text -> {
			text.center(getWidth(), getHeight() - ground.getHeight());
			text.draw(g);
		});

		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		g.drawString(format("%s: (%s)  Bird: Flight: (%s) Sanity: (%s)", control.getDescription(), control.stateID(),
				bird.getFlightState(), bird.getHealthState()), 20, getHeight() - 50);
	}
}