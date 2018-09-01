package de.amr.games.birdy.entities.bird;

import static de.amr.easy.game.Application.CLOCK;
import static de.amr.games.birdy.entities.bird.FlightState.Crashing;
import static de.amr.games.birdy.entities.bird.FlightState.Flying;
import static de.amr.games.birdy.entities.bird.FlightState.OnGround;
import static de.amr.games.birdy.entities.bird.HealthState.Dead;
import static de.amr.games.birdy.entities.bird.HealthState.Injured;
import static de.amr.games.birdy.entities.bird.HealthState.Sane;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdCrashed;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.play.BirdyGameEvent.BirdTouchedPipe;
import static java.lang.Math.PI;

import java.awt.geom.Rectangle2D;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntityUsingSprites;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.AnimationType;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGameApp;
import de.amr.games.birdy.play.BirdyGameEvent;

/**
 * The little bird.
 * 
 * @author Armin Reichert
 */
public class Bird extends GameEntityUsingSprites {

	private final BirdyGameApp app;
	private final FlightControl flightControl;
	private final HealthControl healthControl;
	private float gravity;

	/**
	 * State machine controlling the health state of the bird.
	 */
	private class HealthControl extends StateMachine<HealthState, BirdyGameEvent> {

		public HealthControl() {
			super("Bird Health Control", HealthState.class, Sane);

			changeOnInput(BirdTouchedPipe, Sane, Injured);
			changeOnInput(BirdTouchedGround, Sane, Dead);
			changeOnInput(BirdLeftWorld, Sane, Dead);

			state(Sane).entry = s -> {
				setCurrentSprite("s_yellow");
			};

			state(Injured).entry = s -> {
				s.setDuration(CLOCK.sec(app.settings.get("bird injured seconds")));
				setCurrentSprite("s_red");
			};

			state(Dead).entry = s -> {
				setCurrentSprite("s_blue");
			};

			changeOnInput(BirdTouchedPipe, Injured, Injured, t -> t.from().resetTimer());

			changeOnTimeout(Injured, Sane);

			changeOnInput(BirdTouchedGround, Injured, Dead);

			changeOnInput(BirdLeftWorld, Injured, Dead);

			state(Dead).entry = s -> turnDown();
		}
	}

	/**
	 * State machine controlling the flight state of the bird.
	 */
	private class FlightControl extends StateMachine<FlightState, BirdyGameEvent> {

		public FlightControl() {
			super("Bird Flight Control", FlightState.class, Flying);

			state(Flying).update = s -> {
				if (Keyboard.keyDown(app.settings.get("jump key"))) {
					flap();
				} else {
					fly();
				}
			};

			changeOnInput(BirdCrashed, Flying, Crashing);
			changeOnInput(BirdLeftWorld, Flying, Crashing);
			changeOnInput(BirdTouchedGround, Flying, OnGround);

			state(Crashing).entry = s -> turnDown();
			state(Crashing).update = s -> fall(3);

			changeOnInput(BirdTouchedGround, Crashing, OnGround);

			state(OnGround).entry = s -> {
				Assets.sound("sfx/die.mp3").play();
				turnDown();
			};
		}
	}

	public Bird(BirdyGameApp app) {
		this.app = app;
		flightControl = new FlightControl();
		flightControl.setLogger(Application.LOGGER);
		healthControl = new HealthControl();
		healthControl.setLogger(Application.LOGGER);
		setSprite("s_yellow", createFeatherSprite("bird0"));
		setSprite("s_blue", createFeatherSprite("bird1"));
		setSprite("s_red", createFeatherSprite("bird2"));
		setCurrentSprite("s_yellow");
		tf.setWidth(currentSprite().getWidth());
		tf.setHeight(currentSprite().getHeight());
		gravity = app.settings.getAsFloat("world gravity");
	}

	private Sprite createFeatherSprite(String birdName) {
		Sprite sprite = Sprite.ofAssets(birdName + "_0", birdName + "_1", birdName + "_2");
		sprite.animate(AnimationType.BACK_AND_FORTH, app.settings.get("bird flap millis"));
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
		currentSprite().enableAnimation(tf.getVelocityY() < 0);
	}

	public void receiveEvent(BirdyGameEvent event) {
		flightControl.addInput(event);
		healthControl.addInput(event);
	}

	public FlightState getFlightState() {
		return flightControl.stateID();
	}

	public HealthState getHealthState() {
		return healthControl.stateID();
	}

	@Override
	public Rectangle2D getCollisionBox() {
		int margin = Math.min(tf.getWidth() / 4, tf.getHeight() / 4);
		return new Rectangle2D.Double(tf.getX() + margin, tf.getY() + margin,
				tf.getWidth() - 2 * margin, tf.getHeight() - 2 * margin);
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