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
			if (Keyboard.keyDown(app.settings.get("jump key"))) {
				bird.jump();
			} else {
				bird.fly();
			}
		};
		
		changeOnInput(BirdCrashed, Flying, Crashing);
		
		changeOnInput(BirdLeftWorld, Flying, Crashing);
		
		changeOnInput(BirdTouchedGround, Flying, OnGround);

		state(Crashing).entry = s -> bird.lookDead();
		
		state(Crashing).update = s -> bird.fall(2);
		
		changeOnInput(BirdTouchedGround, Crashing, OnGround);

		state(OnGround).entry = s -> {
			app.assets.sound("sfx/die.mp3").play();
			bird.lookDead();
		};
	}
}