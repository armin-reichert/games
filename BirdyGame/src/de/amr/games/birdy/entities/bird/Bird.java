package de.amr.games.birdy.entities.bird;

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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.play.BirdyGame;
import de.amr.games.birdy.play.BirdyGameEvent;

/**
 * The little bird.
 * 
 * @author Armin Reichert
 */
public class Bird extends GameEntity {

	public final Sprite YELLOW_FEATHERS;
	public final Sprite BLUE_FEATHERS;
	public final Sprite RED_FEATHERS;

	private final BirdyGame app;
	private final FlightControl flightControl;
	private final HealthControl healthControl;
	private Sprite normalFeathers;
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

			state(Injured).entry = s -> {
				s.setDuration(app.pulse.secToTicks(app.settings.get("bird injured seconds")));
				setNormalFeathers(RED_FEATHERS);
			};

			changeOnInput(BirdTouchedPipe, Injured, Injured, (e, s, t) -> {
				s.resetTimer();
			});
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
				Assets.OBJECT.sound("sfx/die.mp3").play();
				turnDown();
			};
		}
	}

	public Bird(BirdyGame app) {
		this.app = app;
		flightControl = new FlightControl();
		// flightControl.setLogger(Application.Log);
		healthControl = new HealthControl();
		// health.setLogger(Application.Log);
		YELLOW_FEATHERS = createFeathers("bird0");
		BLUE_FEATHERS = createFeathers("bird1");
		RED_FEATHERS = createFeathers("bird2");
		setSprites(YELLOW_FEATHERS, BLUE_FEATHERS, RED_FEATHERS);
		normalFeathers = YELLOW_FEATHERS;
		gravity = app.settings.getAsFloat("world gravity");
	}

	private Sprite createFeathers(String birdName) {
		Sprite sprite = new Sprite(birdName + "_0", birdName + "_1", birdName + "_2");
		sprite.makeAnimated(AnimationMode.BACK_AND_FORTH, app.settings.get("bird flap millis"));
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
		currentSprite().setAnimationEnabled(tf.getVelocityY() < 0);
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		// g.setColor(Color.BLACK);
		// g.draw(getCollisionBox());
	}

	public void receiveEvent(BirdyGameEvent event) {
		flightControl.addInput(event);
		healthControl.addInput(event);
	}

	public void setNormalFeathers(Sprite sprite) {
		normalFeathers = sprite;
	}

	public FlightState getFlightState() {
		return flightControl.stateID();
	}

	public HealthState getHealthState() {
		return healthControl.stateID();
	}

	@Override
	public Rectangle2D getCollisionBox() {
		int margin = Math.min(getWidth() / 4, getHeight() / 4);
		return new Rectangle2D.Double(tf.getX() + margin, tf.getY() + margin, getWidth() - 2 * margin,
				getHeight() - 2 * margin);
	}

	@Override
	public Sprite currentSprite() {
		return healthControl.is(Injured) ? RED_FEATHERS : normalFeathers;
	}

	public void flap() {
		flap(2.5f);
	}

	public void flap(float force) {
		Assets.OBJECT.sound("sfx/wing.mp3").play();
		tf.setVelocityY(tf.getVelocityY() - force * gravity);
		fly();
	}

	public void fly() {
		if (tf.getY() < -getHeight()) {
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