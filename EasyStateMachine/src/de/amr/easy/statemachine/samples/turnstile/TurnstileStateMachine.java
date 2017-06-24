package de.amr.easy.statemachine.samples.turnstile;

import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.COIN;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.PASS;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileState.LOCKED;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileState.UNLOCKED;

import de.amr.easy.statemachine.StateMachine;

/**
 * Encapsulates the state machine for the turnstile.
 * 
 * @author Armin Reichert
 */
public class TurnstileStateMachine extends StateMachine<TurnstileState, TurnstileEvent> {

	public TurnstileStateMachine(TurnstileController controller) {
		super("TurnstileStateMachine", TurnstileState.class, TurnstileState.LOCKED);
		changeOnInput(PASS, LOCKED, LOCKED, (before, after) -> controller.alarm());
		changeOnInput(COIN, LOCKED, UNLOCKED, (before, after) -> controller.unlock());
		changeOnInput(PASS, UNLOCKED, LOCKED, (before, after) -> controller.lock());
		changeOnInput(COIN, UNLOCKED, UNLOCKED, (before, after) -> controller.thankyou());
	}
}
