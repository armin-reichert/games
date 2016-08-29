package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.GameEvent.Tick;
import static de.amr.games.birdy.Globals.BIRD_JUMP_SPEED;
import static de.amr.games.birdy.Globals.WORLD_GRAVITY;

import java.awt.Rectangle;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMEventDispatcher;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.entity.GameEntity;
import de.amr.easy.game.sprite.Sprite;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.assets.BirdySound;

public class Bird extends GameEntity implements FSMEventDispatcher<GameEvent> {

	private final FSM<FlightState, GameEvent> flight;
	private final FSM<HealthState, GameEvent> health;
	private Feathers feathers;

	public Bird(Score score) {
		super(Feathers.YELLOW.getSprite(), Feathers.BLUE.getSprite(), Feathers.RED.getSprite());
		feathers = Feathers.YELLOW;
		flight = new FlightControl(this);
		health = new HealthControl(this);
	}

	@Override
	public void init() {
		health.init();
		flight.init();
	}

	@Override
	public void update() {
		flight.run(Tick);
		health.run(Tick);
	}

	@Override
	public void dispatch(GameEvent event) {
		if (flight.isEventAccepted(event)) {
			flight.enqueue(event);
		}
		if (health.isEventAccepted(event)) {
			health.enqueue(event);
		}
	}

	public FlightState getFlightState() {
		return flight.getCurrentState();
	}

	public HealthState getHealthState() {
		return health.getCurrentState();
	}

	@Override
	public Rectangle getCollisionBox() {
		Rectangle r = new Rectangle((int) tr.getX(), (int) tr.getY(), getWidth(), getHeight());
		r.grow(-8, -15);
		return r;
	}

	@Override
	public Sprite currentSprite() {
		return feathers.getSprite();
	}

	public Feathers getFeathers() {
		return feathers;
	}

	public void setFeathers(Feathers feathers) {
		this.feathers = feathers;
	}

	public void jump() {
		jump(1.0f);
	}

	public void jump(float force) {
		BirdySound.BIRD_WING.play();
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