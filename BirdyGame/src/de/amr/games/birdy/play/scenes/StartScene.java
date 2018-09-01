package de.amr.games.birdy.play.scenes;

import static de.amr.easy.game.Application.CLOCK;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.play.scenes.StartScene.State.GameOver;
import static de.amr.games.birdy.play.scenes.StartScene.State.Ready;
import static de.amr.games.birdy.play.scenes.StartScene.State.StartPlaying;
import static de.amr.games.birdy.play.scenes.StartScene.State.Starting;
import static de.amr.games.birdy.utils.Util.randomInt;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.controls.PumpingImage;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.entity.collision.Collision;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.view.Controller;
import de.amr.easy.game.view.View;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGameApp;
import de.amr.games.birdy.entities.Area;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.GraphicText;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.play.BirdyGameEvent;

/**
 * Start scene of the game: bird flaps in the air until user presses the JUMP key.
 * 
 * @author Armin Reichert
 */
public class StartScene implements View, Controller {

	public enum State {
		Starting, Ready, GameOver, StartPlaying, StartSpriteBrowser
	}

	private class StartSceneControl extends StateMachine<State, BirdyGameEvent> {

		public StartSceneControl() {
			super("Start Scene Control", State.class, Starting);

			// Starting ---

			state(Starting).entry = s -> {
				reset();
				if (!Assets.sound("music/bgmusic.mp3").isRunning()) {
					Assets.sound("music/bgmusic.mp3").loop();
				}
			};

			state(Starting).update = s -> keepBirdInAir();

			change(Starting, Ready, () -> Keyboard.keyDown(app.settings.get("jump key")));

			changeOnInput(BirdTouchedGround, Starting, GameOver);

			// Ready ---

			state(Ready).entry = s -> {
				s.setDuration(CLOCK.secToTicks(app.settings.getAsFloat("ready time sec")));
				displayText("readyText");
			};

			changeOnTimeout(Ready, StartPlaying, t -> app.setController(app.getPlayScene()));

			changeOnInput(BirdTouchedGround, Ready, GameOver, t -> displayText("title"));

			state(Ready).exit = s -> displayedText = null;

			// GameOver ---

			state(GameOver).entry = s -> {
				stop();
				displayedText = app.entities.ofName("game_over");
				Assets.sounds().forEach(Sound::stop);
			};

			change(GameOver, Starting, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		}
	}

	private final BirdyGameApp app;
	private final StartSceneControl control;
	private Bird bird;
	private City city;
	private Ground ground;
	private GraphicText displayedText;

	public StartScene(BirdyGameApp game) {
		this.app = game;
		control = new StartSceneControl();
	}

	public int getWidth() {
		return app.settings.width;
	}

	public int getHeight() {
		return app.settings.height;
	}

	@Override
	public void init() {
		// control.setLogger(Application.LOG);
		control.init();
	}

	private void stop() {
		ground.tf.setVelocity(0, 0);
	}

	private void keepBirdInAir() {
		while (bird.tf.getY() > ground.tf.getY() / 2) {
			bird.flap(randomInt(1, 4));
		}
	}

	private void displayText(String name) {
		displayedText = app.entities.ofName(name);
	}

	private void reset() {
		city = app.entities.ofClass(City.class).findAny().get();
		city.setWidth(getWidth());
		city.init();

		ground = app.entities.ofClass(Ground.class).findAny().get();
		ground.setWidth(getWidth());
		ground.tf.setPosition(0, getHeight() - ground.tf.getHeight());
		ground.tf.setVelocity(app.settings.getAsFloat("world speed"), 0);

		bird = app.entities.ofClass(Bird.class).findAny().get();
		bird.init();
		bird.tf.setPosition(getWidth() / 8, ground.tf.getY() / 2);
		bird.tf.setVelocity(0, 0);

		if (!app.entities.contains("title")) {
			GameEntity titleText = new GraphicText(Assets.image("title"));
			app.entities.store("title", titleText);
		}

		if (!app.entities.contains("text_game_over")) {
			GameEntity gameOverText = new GraphicText(Assets.image("text_game_over"));
			app.entities.store("text_game_over", gameOverText);
		}

		if (!app.entities.contains("text_ready")) {
			PumpingImage readyText = PumpingImage.create().image(Assets.image("text_ready")).build();
			app.entities.store("text_ready", readyText);
		}

		if (!app.entities.contains("world")) {
			Area world = new Area(getWidth(), 2 * getHeight());
			world.tf.setPosition(0, -getHeight());
			app.entities.store("world", world);
		}

		app.collisionHandler.clear();
		app.collisionHandler.registerEnd(bird, app.entities.ofClass(Area.class).findAny().get(),
				BirdLeftWorld);
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
		if (displayedText != null) {
			displayedText.tf.center(getWidth(), getHeight() - ground.tf.getHeight());
			displayedText.draw(g);
		}
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		g.drawString(format("%s: (%s)  Bird: Flight: (%s) Sanity: (%s)", control.getDescription(),
				control.stateID(), bird.getFlightState(), bird.getHealthState()), 20, getHeight() - 50);
	}
}