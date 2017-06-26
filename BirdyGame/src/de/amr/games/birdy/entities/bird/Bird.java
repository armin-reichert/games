package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.BirdyGameGlobals.BIRD_FLAP_DURATION_MILLIS;
import static de.amr.games.birdy.BirdyGameGlobals.BIRD_JUMP_SPEED;
import static de.amr.games.birdy.BirdyGameGlobals.WORLD_GRAVITY;

import java.awt.Rectangle;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.AnimationMode;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.BirdyGameEvent;

public class Bird extends GameEntity {

	public final Sprite YELLOW_FEATHERS;
	public final Sprite BLUE_FEATHERS;
	public final Sprite RED_FEATHERS;
	
	private final BirdyGame app;
	private final FlightControl flightControl;
	private final HealthControl healthControl;
	private Sprite currentSprite;

	public Bird(BirdyGame app, Score score) {
		this.app = app;
		YELLOW_FEATHERS = createFlapSprite("bird0");
		BLUE_FEATHERS = createFlapSprite("bird1");
		RED_FEATHERS = createFlapSprite("bird2");
		setSprites(YELLOW_FEATHERS, BLUE_FEATHERS, RED_FEATHERS);
		currentSprite = YELLOW_FEATHERS;
		flightControl = new FlightControl(app, this);
		healthControl = new HealthControl(this);

		// Tracing
//		flightControl.setLogger(Application.Log);
//		health.setLogger(Application.Log);
	}

	private Sprite createFlapSprite(String birdName) {
		Sprite sprite = new Sprite(app.assets, birdName + "_0", birdName + "_1", birdName + "_2");
		sprite.createAnimation(AnimationMode.BACK_AND_FORTH, BIRD_FLAP_DURATION_MILLIS);
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
	public Rectangle getCollisionBox() {
		Rectangle r = new Rectangle((int) tr.getX(), (int) tr.getY(), getWidth(), getHeight());
		r.grow(-8, -15);
		return r;
	}

	@Override
	public Sprite currentSprite() {
		return currentSprite;
	}

	public void jump() {
		jump(1.0f);
	}

	public void jump(float force) {
		app.BIRD_WING.play();
		tr.setVelY(tr.getVelY() - force * BIRD_JUMP_SPEED);
		fly();
	}

	void fly() {
		if (tr.getY() < -getHeight()) {
			tr.setVel(0, 0);
		}
		tr.setVelY(tr.getVelY() + WORLD_GRAVITY);
		double damp = tr.getVelY() < 0 ? 0.05 : 0.2;
		tr.setRot(-Math.PI / 8 + damp * tr.getVelY());
		if (tr.getRot() < -Math.PI / 4)
			tr.setRot(-Math.PI / 4);
		if (tr.getRot() > Math.PI / 2)
			tr.setRot(Math.PI / 2);
		tr.move();
		updateAnimation();
	}

	void fall(float slowdown) {
		tr.setVelY(tr.getVelY() + WORLD_GRAVITY / slowdown);
		tr.move();
		updateAnimation();
	}

	void lookDead() {
		tr.setRot(Math.PI / 2);
		tr.setVel(0, 0);
		updateAnimation();
	}

	void lookFlying() {
		updateAnimation();
	}

	private void updateAnimation() {
		currentSprite().setAnimated(tr.getVelY() < 0);
	}
}