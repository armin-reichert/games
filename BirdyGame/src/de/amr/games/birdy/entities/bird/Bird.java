package de.amr.games.birdy.entities.bird;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.easy.game.Application.app;
import static de.amr.games.birdy.entities.bird.FlightState.Crashing;
import static de.amr.games.birdy.entities.bird.FlightState.Flying;
import static de.amr.games.birdy.entities.bird.FlightState.OnGround;
import static de.amr.games.birdy.entities.bird.HealthState.Dead;
import static de.amr.games.birdy.entities.bird.HealthState.Injured;
import static de.amr.games.birdy.entities.bird.HealthState.Sane;
import static de.amr.games.birdy.play.BirdEvent.BirdCrashed;
import static de.amr.games.birdy.play.BirdEvent.BirdLeftPassage;
import static de.amr.games.birdy.play.BirdEvent.BirdLeftWorld;
import static de.amr.games.birdy.play.BirdEvent.BirdTouchedGround;
import static de.amr.games.birdy.play.BirdEvent.BirdTouchedPipe;
import static java.lang.Math.PI;

import java.awt.geom.Rectangle2D;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.SpriteEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.ui.sprites.AnimationType;
import de.amr.easy.game.ui.sprites.Sprite;
import de.amr.games.birdy.play.BirdEvent;
import de.amr.statemachine.Match;
import de.amr.statemachine.StateMachine;

/**
 * The little bird.
 * 
 * @author Armin Reichert
 */
public class Bird extends SpriteEntity {

	private final FlightControl flightControl;
	private final HealthControl healthControl;
	private float gravity;

	/**
	 * State machine controlling the health state of the bird.
	 */
	private class HealthControl extends StateMachine<HealthState, BirdEvent> {

		public HealthControl() {

			super(HealthState.class, Match.BY_EQUALITY);
			setDescription("Bird Health Control");
			setInitialState(Sane);

			state(Sane).setOnEntry(() -> sprites.select("s_yellow"));

			addTransitionOnEventObject(Sane, Sane, null, null, BirdLeftPassage);
			addTransitionOnEventObject(Sane, Injured, null, null, BirdTouchedPipe);
			addTransitionOnEventObject(Sane, Dead, null, null, BirdTouchedGround);
			addTransitionOnEventObject(Sane, Dead, null, null, BirdLeftWorld);

			state(Injured).setTimer(() -> app().clock.sec(app().settings.get("bird injured seconds")));
			state(Injured).setOnEntry(() -> sprites.select("s_red"));

			addTransitionOnEventObject(Injured, Injured, null, e -> state(Injured).resetTimer(), BirdTouchedPipe);
			addTransitionOnTimeout(Injured, Sane, null, null);
			addTransitionOnEventObject(Injured, Injured, null, null, BirdCrashed);
			addTransitionOnEventObject(Injured, Injured, null, null, BirdLeftPassage);
			addTransitionOnEventObject(Injured, Dead, null, null, BirdTouchedGround);
			addTransitionOnEventObject(Injured, Dead, null, null, BirdLeftWorld);

			state(Dead).setOnEntry(() -> {
				sprites.select("s_blue");
				turnDown();
			});

			addTransitionOnEventObject(Dead, Dead, null, null, BirdTouchedGround);
		}
	}

	/**
	 * State machine controlling the flight state of the bird.
	 */
	private class FlightControl extends StateMachine<FlightState, BirdEvent> {

		public FlightControl() {

			super(FlightState.class, Match.BY_EQUALITY);
			setDescription("Bird Flight Control");
			setInitialState(Flying);

			state(Flying).setOnTick(() -> {
				if (Keyboard.keyDown(app().settings.get("jump key"))) {
					flap();
				} else {
					fly();
				}
			});

			addTransitionOnEventObject(Flying, Flying, null, null, BirdLeftPassage);
			addTransitionOnEventObject(Flying, Crashing, null, null, BirdTouchedPipe);
			addTransitionOnEventObject(Flying, Crashing, null, null, BirdCrashed);
			addTransitionOnEventObject(Flying, Crashing, null, null, BirdLeftWorld);
			addTransitionOnEventObject(Flying, OnGround, null, null, BirdTouchedGround);

			state(Crashing).setOnEntry(() -> turnDown());
			state(Crashing).setOnTick(() -> fall(3));

			addTransitionOnEventObject(Crashing, Crashing, null, null, BirdCrashed);
			addTransitionOnEventObject(Crashing, Crashing, null, null, BirdLeftPassage);
			addTransitionOnEventObject(Crashing, Crashing, null, null, BirdTouchedPipe);
			addTransitionOnEventObject(Crashing, OnGround, null, null, BirdTouchedGround);

			state(OnGround).setOnEntry(() -> {
				Assets.sound("sfx/die.mp3").play();
				turnDown();
			});

			addTransitionOnEventObject(OnGround, OnGround, null, null, BirdTouchedGround);
		}
	}

	public Bird() {
		flightControl = new FlightControl();
		flightControl.traceTo(LOGGER, app().clock::getFrequency);
		healthControl = new HealthControl();
		healthControl.traceTo(LOGGER, app().clock::getFrequency);
		sprites.set("s_yellow", createFeatherSprite("bird0"));
		sprites.set("s_blue", createFeatherSprite("bird1"));
		sprites.set("s_red", createFeatherSprite("bird2"));
		sprites.select("s_yellow");
		tf.setWidth(sprites.current().getWidth());
		tf.setHeight(sprites.current().getHeight());
		gravity = app().settings.getAsFloat("world gravity");
	}

	private Sprite createFeatherSprite(String birdName) {
		Sprite sprite = Sprite.ofAssets(birdName + "_0", birdName + "_1", birdName + "_2");
		sprite.animate(AnimationType.BACK_AND_FORTH, app().settings.get("bird flap millis"));
		return sprite;
	}

	@Override
	public void init() {
		healthControl.init();
		flightControl.init();
	}

	@Override
	public void update() {
		flightControl.update();
		healthControl.update();
		sprites.current().enableAnimation(tf.getVelocityY() < 0);
	}

	public void receiveEvent(BirdEvent event) {
		flightControl.enqueue(event);
		healthControl.enqueue(event);
	}

	public FlightState getFlightState() {
		return flightControl.getState();
	}

	public HealthState getHealthState() {
		return healthControl.getState();
	}

	@Override
	public Rectangle2D getCollisionBox() {
		int margin = Math.min(tf.getWidth() / 4, tf.getHeight() / 4);
		return new Rectangle2D.Double(tf.getX() + margin, tf.getY() + margin, tf.getWidth() - 2 * margin,
				tf.getHeight() - 2 * margin);
	}

	public void flap() {
		flap(2.5f);
	}

	public void flap(float force) {
		Assets.sound("sfx/wing.mp3").play();
		tf.setVelocityY(tf.getVelocityY() - force * gravity);
		fly();
	}

	public void fly() {
		if (tf.getY() < -tf.getHeight()) {
			tf.setVelocity(0, 0);
		}
		tf.setVelocityY(tf.getVelocityY() + gravity);
		double damp = tf.getVelocityY() < 0 ? 0.05 : 0.2;
		tf.setRotation(-PI / 8 + damp * tf.getVelocityY());
		if (tf.getRotation() < -PI / 4)
			tf.setRotation(-PI / 4);
		if (tf.getRotation() > PI / 2)
			tf.setRotation(PI / 2);
		tf.move();
	}

	public void fall(float slowdown) {
		tf.setVelocityY(tf.getVelocityY() + gravity / slowdown);
		tf.move();
	}

	private void turnDown() {
		tf.setRotation(PI / 2);
		tf.setVelocity(0, 0);
	}
}