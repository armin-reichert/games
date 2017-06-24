package de.amr.easy.statemachine.samples.turnstile;

import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.COIN;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.PASS;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileState.LOCKED;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileState.UNLOCKED;

import java.util.EnumMap;
import java.util.logging.Logger;

import de.amr.easy.statemachine.StateMachine;

/**
 * Sample taken from
 * https://www.linkedin.com/pulse/automata-based-programming-general-purpose-finite-state-kolarova-1.
 * 
 * @author Armin Reichert
 */
public class Turnstile {

	private static final Logger LOGGER = Logger.getGlobal();

	private final StateMachine<TurnstileState, TurnstileEvent> fsm;

	public Turnstile(TurnstileController controller) {
		fsm = new StateMachine<>("Turnstile", new EnumMap<>(TurnstileState.class), LOCKED);
		fsm.changeOnInput(PASS, LOCKED, LOCKED, (before, after) -> controller.alarm());
		fsm.changeOnInput(COIN, LOCKED, UNLOCKED, (before, after) -> controller.unlock());
		fsm.changeOnInput(PASS, UNLOCKED, LOCKED, (before, after) -> controller.lock());
		fsm.changeOnInput(COIN, UNLOCKED, UNLOCKED, (before, after) -> controller.thankyou());
		// fsm.setLogger(LOGGER);
	}

	public void init() {
		fsm.init();
	}

	public void event(TurnstileEvent event) {
		fsm.addInput(event);
		fsm.update();
	}

	public static void main(String[] args) {
		Turnstile ts = new Turnstile(new MockController(LOGGER));
		ts.init();
		ts.event(COIN);
		ts.event(PASS);
		ts.event(COIN);
		ts.event(PASS);
		ts.event(PASS);
	}
}