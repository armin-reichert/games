package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.BirdyGameEvent.BirdCrashed;
import static de.amr.games.birdy.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.entities.bird.FlightState.Crashing;
import static de.amr.games.birdy.entities.bird.FlightState.Flying;
import static de.amr.games.birdy.entities.bird.FlightState.OnGround;
import static de.amr.games.birdy.entities.bird.HealthState.Dead;
import static de.amr.games.birdy.entities.bird.HealthState.Injured;
import static de.amr.games.birdy.entities.bird.HealthState.Sane;
import static java.lang.Math.PI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.BirdyGameEvent;

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
	private Sprite currentSprite;
	private float gravity;

	/**
	 * State machine controlling the health state of the bird.
	 */
	private class HealthControl extends StateMachine<HealthState, BirdyGameEvent> {

		private Sprite feathersBeforeInjury;

		public HealthControl() {
			super("Bird Health Control", HealthState.class, Sane);

			changeOnInput(BirdTouchedPipe, Sane, Injured);
			changeOnInput(BirdTouchedGround, Sane, Dead);
			changeOnInput(BirdLeftWorld, Sane, Dead);

			state(Injured).entry = s -> {
				s.setDuration(app.motor.secToTicks(app.settings.get("bird injured seconds")));
				feathersBeforeInjury = currentSprite();
				setFeathers(RED_FEATHERS);
			};
			state(Injured).exit = s -> setFeathers(feathersBeforeInjury);

			changeOnInput(BirdTouchedPipe, Injured, Injured, (s, t) -> {
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
				app.assets.sound("sfx/die.mp3").play();
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
		currentSprite = YELLOW_FEATHERS;
		gravity = app.settings.getFloat("world gravity");
	}

	private Sprite createFeathers(String birdName) {
		Sprite sprite = new Sprite(app.assets, birdName + "_0", birdName + "_1", birdName + "_2");
		sprite.createAnimation(AnimationMode.BACK_AND_FORTH, app.settings.get("bird flap millis"));
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
		currentSprite().setAnimated(tr.getVelocityY() < 0);
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		drawCollisionBox(g, Color.BLACK);
	}

	public void receiveEvent(BirdyGameEvent event) {
		flightControl.addInput(event);
		healthControl.addInput(event);
	}

	public void setFeathers(Sprite sprite) {
		currentSprite = sprite;
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
		return new Rectangle2D.Double(tr.getX() + margin, tr.getY() + margin, getWidth() - 2 * margin,
				getHeight() - 2 * margin);
	}

	@Override
	public Sprite currentSprite() {
		return currentSprite;
	}

	public void flap() {
		flap(2.5f);
	}

	public void flap(float force) {
		app.assets.sound("sfx/wing.mp3").play();
		tr.setVelocityY(tr.getVelocityY() - force * gravity);
		fly();
	}

	public void fly() {
		if (tr.getY() < -getHeight()) {
			tr.setVelocity(0, 0);
		}
		tr.setVelocityY(tr.getVelocityY() + gravity);
		double damp = tr.getVelocityY() < 0 ? 0.05 : 0.2;
		tr.setRotation(-PI / 8 + damp * tr.getVelocityY());
		if (tr.getRotation() < -PI / 4)
			tr.setRotation(-PI / 4);
		if (tr.getRotation() > PI / 2)
			tr.setRotation(PI / 2);
		tr.move();
	}

	public void fall(float slowdown) {
		tr.setVelocityY(tr.getVelocityY() + gravity / slowdown);
		tr.move();
	}

	private void turnDown() {
		tr.setRotation(PI / 2);
		tr.setVelocity(0, 0);
	}
}