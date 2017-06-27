package de.amr.games.birdy.scenes.start;

import static de.amr.games.birdy.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.scenes.start.StartSceneState.GameOver;
import static de.amr.games.birdy.scenes.start.StartSceneState.Ready;
import static de.amr.games.birdy.scenes.start.StartSceneState.StartPlaying;
import static de.amr.games.birdy.scenes.start.StartSceneState.Starting;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.lang.String.format;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.Random;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.PumpingText;
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
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.TitleText;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.play.PlayScene;
import de.amr.games.birdy.utils.Util;

/**
 * Start scene of the game: bird flaps in the air until user presses the JUMP key.
 * 
 * @author Armin Reichert
 */
public class StartScene extends Scene<BirdyGame> {

	private class StartSceneControl extends StateMachine<StartSceneState, BirdyGameEvent> {

		public StartSceneControl() {
			super("Start Scene Control", StartSceneState.class, Starting);

			// Starting
			state(Starting).entry = s -> reset();
			
			state(Starting).update = s -> keepBirdInAir();
			
			change(Starting, Ready, () -> Keyboard.keyDown(app.settings.get("jump key")));
			
			changeOnInput(BirdTouchedGround, Starting, GameOver);

			// Ready
			state(Ready).entry = s -> {
				s.setDuration(app.motor.secToTicks(app.settings.getFloat("ready time sec")));
				showText(app.entities.findByName(PumpingText.class, "readyText"));
			};
			
			state(Ready).exit = s -> hideText();
			
			changeOnTimeout(Ready, StartPlaying, (s, t) -> app.views.show(PlayScene.class));
			
			changeOnInput(BirdTouchedGround, Ready, GameOver, (s, t) -> showText(app.entities.findAny(TitleText.class)));

			// GameOver
			state(GameOver).entry = s -> stopScrolling();
			
			change(GameOver, Starting, () -> Keyboard.keyPressedOnce(VK_SPACE));
		}
	}

	private final StartSceneControl control;
	private Bird bird;
	private City city;
	private Ground ground;
	private GameEntity displayedText;

	public StartScene(BirdyGame game) {
		super(game);
		control = new StartSceneControl();
		 control.setLogger(Application.LOG);
	}

	@Override
	public void init() {
		control.init();
	}

	private void hideText() {
		displayedText = null;
	}

	private void showText(GameEntity text) {
		displayedText = text;
	}

	private void stopScrolling() {
		ground.tr.setVelocity(0, 0);
	}

	private void keepBirdInAir() {
		while (bird.tr.getY() > ground.tr.getY() / 2) {
			bird.jump(Util.randomInt(1, 4));
		}
	}

	private void reset() {
		city = app.entities.findAny(City.class);
		city.setWidth(getWidth());
		city.setNight(new Random().nextBoolean());
		ground = app.entities.findAny(Ground.class);
		ground.setWidth(getWidth());
		ground.tr.moveTo(0, getHeight() - ground.getHeight());
		ground.tr.setVelocity(app.settings.getFloat("world speed"), 0);
		bird = app.entities.findAny(Bird.class);
		bird.init();
		bird.tr.moveTo(getWidth() / 8, ground.tr.getY() / 2);
		bird.tr.setVelocity(0, 0);
		bird.setFeathers(city.isNight() ? bird.BLUE_FEATHERS : bird.YELLOW_FEATHERS);
		displayedText = app.entities.add(new TitleText(app.assets));
		PumpingText readyText = new PumpingText(app, "text_ready", 0.2f);
		readyText.setName("readyText");
		app.entities.add(readyText);
		CollisionHandler.clear();
		CollisionHandler.detectCollisionStart(bird, ground, BirdTouchedGround);
		Area birdWorld = new Area(0, -getHeight(), getWidth(), 2 * getHeight());
		CollisionHandler.detectCollisionEnd(bird, birdWorld, BirdLeftWorld);
	}

	@Override
	public void update() {
		city.update();
		ground.update();
		bird.update();
		for (Collision ce : CollisionHandler.collisions()) {
			BirdyGameEvent event = (BirdyGameEvent) ce.getAppEvent();
			bird.receiveEvent(event);
			control.addInput(event);
		}
		control.update();
	}

	@Override
	public void draw(Graphics2D g) {
		city.draw(g);
		ground.draw(g);
		bird.draw(g);
		if (displayedText != null) {
			displayedText.center(getWidth(), getHeight() - ground.getHeight());
			displayedText.draw(g);
		}
		// debugging: show state
		showState(g);
	}

	private void showState(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
		g.drawString(format("%s: %s  Bird: %s & %s", control.getDescription(), control.stateID(), bird.getFlightState(),
				bird.getHealthState()), 20, getHeight() - 50);
	}
}