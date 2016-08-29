package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.GameEvent.BirdLeftWorld;
import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.GameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.GameEvent.Tick;
import static de.amr.games.birdy.Globals.BIRD_INJURED_TIME;
import static de.amr.games.birdy.entities.bird.HealthState.Dead;
import static de.amr.games.birdy.entities.bird.HealthState.Injured;
import static de.amr.games.birdy.entities.bird.HealthState.Sane;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.easy.game.timing.Countdown;
import de.amr.games.birdy.GameEvent;

public class HealthControl extends FSM<HealthState, GameEvent> {

	private final Countdown injuredTimer = new Countdown(BIRD_INJURED_TIME);
	private Feathers saneFeathers;

	@Override
	protected Map<HealthState, FSMState<HealthState, GameEvent>> createStateMap() {
		return new EnumMap<>(HealthState.class);
	}

	public HealthControl(Bird bird) {
		//@formatter:off
		beginFSM()
			.description("Bird Health")
			.acceptedEvents(EnumSet.of(Tick, BirdTouchedPipe, BirdTouchedGround, BirdLeftWorld))
			.defaultEvent(Tick)
			.initialState(Sane)
			
			.state(Sane)
				.keep()
				.into(Injured).on(BirdTouchedPipe)
				.into(Dead).on(BirdTouchedGround)
				.into(Dead).on(BirdLeftWorld)
			.end()
			
			.state(Injured)
				.entering(() -> {
					injuredTimer.restart();
					saneFeathers = bird.getFeathers();
					bird.setFeathers(Feathers.RED);
				})
				.exiting(() -> {
					bird.setFeathers(saneFeathers);
				})
				.keep().on(BirdTouchedPipe).act(injuredTimer::restart)
				.keep().act(injuredTimer::update)
				.into(Sane).when(injuredTimer::isComplete)
				.into(Dead).on(BirdTouchedGround)
				.into(Dead).on(BirdLeftWorld)
			.end()
			
			.state(Dead)
				.entering(bird::lookDead)
				.keep().on(BirdTouchedGround)
				.keep().on(BirdLeftWorld)
				.keep()
			.end()
			
		.endFSM();
		//@formatter:on
	}
}