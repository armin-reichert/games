package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.BirdyGameEvent.BirdCrashed;
import static de.amr.games.birdy.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.entities.bird.FlightState.Crashing;
import static de.amr.games.birdy.entities.bird.FlightState.Flying;
import static de.amr.games.birdy.entities.bird.FlightState.OnGround;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.BirdyGameEvent;

public class FlightControl extends StateMachine<FlightState, BirdyGameEvent> {

	public FlightControl(BirdyGame app, Bird bird) {
		super("Bird Flight Control", FlightState.class, Flying);

		state(Flying).entry = s -> bird.lookFlying();
		state(Flying).update = s -> {
			if (Keyboard.keyDown(BirdyGame.JUMP_KEY)) {
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
			app.SND_BIRD_DIES.play();
			bird.lookDead();
		};
	}
}