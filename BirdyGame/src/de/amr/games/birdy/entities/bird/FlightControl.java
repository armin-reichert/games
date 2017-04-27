package de.amr.games.birdy.entities.bird;

import static de.amr.games.birdy.GameEvent.BirdCrashed;
import static de.amr.games.birdy.GameEvent.BirdLeftWorld;
import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.GameEvent.Tick;
import static de.amr.games.birdy.Globals.JUMP_KEY;
import static de.amr.games.birdy.entities.bird.FlightState.Crashing;
import static de.amr.games.birdy.entities.bird.FlightState.Flying;
import static de.amr.games.birdy.entities.bird.FlightState.OnGround;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.easy.game.input.Keyboard;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.assets.BirdySound;

public class FlightControl extends FSM<FlightState, GameEvent> {

	@Override
	protected Map<FlightState, FSMState<FlightState, GameEvent>> createStateMap() {
		return new EnumMap<>(FlightState.class);
	}

	public FlightControl(Bird bird) {
		//@formatter:off
		beginFSM()
			.description("Bird Flight")
			.acceptedEvents(EnumSet.of(Tick, BirdTouchedGround, BirdCrashed, BirdLeftWorld))
			.defaultEvent(Tick)
			.initialState(Flying)
			
			.state(Flying)
				.entering(bird::lookFlying)
				.keep().act(bird::fly)
				.keep().when(() -> Keyboard.down(JUMP_KEY)).act(bird::jump)
				.into(OnGround).on(BirdTouchedGround)
				.into(Crashing).on(BirdCrashed)
				.into(Crashing).on(BirdLeftWorld)
			.end()
			
			.state(Crashing)
				.entering(bird::lookDead)
				.keep().act(() -> bird.fall(2))
				.keep().on(BirdLeftWorld)
				.into(OnGround).on(BirdTouchedGround) 
			.end()
			
			.state(OnGround)
				.entering(() -> {BirdySound.BIRD_DIES.play(); bird.lookDead();})
				.keep().on(BirdTouchedGround)
				.keep()
			.end()
			
		.endFSM();
		//@formatter:on
	}
}
