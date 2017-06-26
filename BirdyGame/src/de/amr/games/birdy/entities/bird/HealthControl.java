package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.BirdyGameEvent.BirdLeftWorld;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedGround;
import static de.amr.games.birdy.BirdyGameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.BirdyGameGlobals.BIRD_INJURED_TIME;
import static de.amr.games.birdy.entities.bird.HealthState.Dead;
import static de.amr.games.birdy.entities.bird.HealthState.Injured;
import static de.amr.games.birdy.entities.bird.HealthState.Sane;

import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGameEvent;

public class HealthControl extends StateMachine<HealthState, BirdyGameEvent> {

	private Sprite feathersBeforeInjury;

	public HealthControl(Bird bird) {
		super("Bird Health Control", HealthState.class, Sane);

		changeOnInput(BirdTouchedPipe, Sane, Injured);
		changeOnInput(BirdTouchedGround, Sane, Dead);
		changeOnInput(BirdLeftWorld, Sane, Dead);

		state(Injured).entry = s -> {
			s.setDuration(BIRD_INJURED_TIME);
			feathersBeforeInjury = bird.currentSprite();
			bird.setFeathers(bird.RED_FEATHERS);
		};
		state(Injured).exit = s -> bird.setFeathers(feathersBeforeInjury);

		changeOnInput(BirdTouchedPipe, Injured, Injured, (s, t) -> {
			s.setDuration(BIRD_INJURED_TIME);
		});
		changeOnTimeout(Injured, Sane);
		changeOnInput(BirdTouchedGround, Injured, Dead);
		changeOnInput(BirdLeftWorld, Injured, Dead);

		state(Dead).entry = s -> bird.lookDead();
	}
}