package de.amr.easy.statemachine.samples.turnstile;

import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.COIN;
import static de.amr.easy.statemachine.samples.turnstile.TurnstileEvent.PASS;

import java.util.logging.Logger;

public class TurnstileApp {

	public static void main(String[] args) {
		Turnstile t = new Turnstile();
		t.init();
		t.event(COIN);
		t.event(PASS);
		t.event(COIN);
		t.event(PASS);
		t.event(PASS);

		t.setController(new TurnstileTraceController(Logger.getGlobal()));
		t.init();
		t.event(COIN);
		t.event(PASS);
		t.event(COIN);
		t.event(PASS);
		t.event(PASS);
	}
}
