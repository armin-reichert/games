package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.GameEvent.BirdLeftWorld;
import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.GameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.Globals.BIRD_INJURED_TIME;
import static de.amr.games.birdy.entities.bird.HealthState.Dead;
import static de.amr.games.birdy.entities.bird.HealthState.Injured;
import static de.amr.games.birdy.entities.bird.HealthState.Sane;

import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.GameEvent;

public class HealthControl extends StateMachine<HealthState, GameEvent> {

	private Feathers saneFeathers;

	public HealthControl(Bird bird) {
		super("Bird Health Control", HealthState.class, Sane);

		changeOnInput(BirdTouchedPipe, Sane, Injured);
		changeOnInput(BirdTouchedGround, Sane, Dead);
		changeOnInput(BirdLeftWorld, Sane, Dead);

		state(Injured).entry = s -> {
			s.setDuration(BIRD_INJURED_TIME);
			saneFeathers = bird.getFeathers();
			bird.setFeathers(Feathers.RED);
		};
		state(Injured).exit = s -> bird.setFeathers(saneFeathers);
		changeOnInput(BirdTouchedPipe, Injured, Injured, (s, t) -> {
			s.setDuration(BIRD_INJURED_TIME);
		});
		changeOnTimeout(Injured, Sane);
		changeOnInput(BirdTouchedGround, Injured, Dead);
		changeOnInput(BirdLeftWorld, Injured, Dead);

		state(Dead).entry = s -> bird.lookDead();
	}
}