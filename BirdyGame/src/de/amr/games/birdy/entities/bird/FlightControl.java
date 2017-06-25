package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.GameEvent.BirdCrashed;
import static de.amr.games.birdy.GameEvent.BirdLeftWorld;
import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.entities.bird.FlightState.Crashing;
import static de.amr.games.birdy.entities.bird.FlightState.Flying;
import static de.amr.games.birdy.entities.bird.FlightState.OnGround;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.Globals;
import de.amr.games.birdy.assets.BirdySound;

public class FlightControl extends StateMachine<FlightState, GameEvent> {

	public FlightControl(Bird bird) {
		super("Bird Flight Control", FlightState.class, Flying);

		state(Flying).entry = s -> bird.lookFlying();
		state(Flying).update = s -> {
			if (Keyboard.keyDown(Globals.JUMP_KEY)) {
				bird.jump();
			} else {
				bird.fly();
			}
		};
		changeOnInput(BirdTouchedGround, Flying, OnGround);
		changeOnInput(BirdCrashed, Flying, Crashing);
		changeOnInput(BirdLeftWorld, Flying, Crashing);

		state(Crashing).entry = s -> bird.lookDead();
		state(Crashing).update = s -> bird.fall(2);
		changeOnInput(BirdTouchedGround, Crashing, OnGround);

		state(OnGround).entry = s -> {
			BirdySound.BIRD_DIES.play();
			bird.lookDead();
		};
	}
}