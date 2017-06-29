package de.amr.easy.statemachine.samples.turnstile;

import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.COIN;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.PASS;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileState.LOCKED;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileState.UNLOCKED;

import de.amr.easy.statemachine.StateMachine;

/**
 * @see https://www.linkedin.com/pulse/automata-based-programming-general-purpose-finite-state-kolarova-1.
 * 
 * @author Armin Reichert
 */
public class TurnstileImplementation implements Turnstile {

	private final StateMachine<TurnstileState, TurnstileEvent> fsm;
	private TurnstileController controller;

	public TurnstileImplementation() {
		this(new TurnstileNullController());
	}

	public TurnstileImplementation(TurnstileController c) {
		controller = c;
		fsm = new StateMachine<>("TurnstileStateMachine", TurnstileState.class, LOCKED);
		fsm.changeOnInput(PASS, LOCKED, LOCKED, (before, after) -> controller.alarm());
		fsm.changeOnInput(COIN, LOCKED, UNLOCKED, (before, after) -> controller.unlock());
		fsm.changeOnInput(PASS, UNLOCKED, LOCKED, (before, after) -> controller.lock());
		fsm.changeOnInput(COIN, UNLOCKED, UNLOCKED, (before, after) -> controller.thankyou());
	}

	public void setController(TurnstileController controller) {
		this.controller = controller;
	}

	public void init() {
		fsm.init();
	}

	@Override
	public void event(TurnstileEvent event) {
		fsm.addInput(event);
		fsm.update();
	}

	@Override
	public TurnstileState getState() {
		return fsm.stateID();
	}
}