package de.amr.easy.statemachine.samples.turnstile;

import java.util.logging.Logger;

/**
 * Sample taken from
 * https://www.linkedin.com/pulse/automata-based-programming-general-purpose-finite-state-kolarova-1.
 * 
 * @author Armin Reichert
 */
public class Turnstile {

	private TurnstileStateMachine fsm;
	
	public Turnstile() {
		this(new TurnstileNullController());
	}

	public Turnstile(TurnstileController controller) {
		fsm = new TurnstileStateMachine(controller);
	}

	public void setController(TurnstileController controller) {
		fsm = new TurnstileStateMachine(controller);
	}

	public void init() {
		fsm.init();
		fsm.setLogger(Logger.getGlobal());
	}

	public void event(TurnstileEvent event) {
		fsm.addInput(event);
		fsm.update();
	}
}