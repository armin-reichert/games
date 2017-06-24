package de.amr.easy.statemachine.samples.turnstile;

import java.util.logging.Logger;

class MockController implements TurnstileController {
	
	private final Logger log;
	
	public MockController(Logger log) {
		this.log = log;
	}

	@Override
	public void lock() {
		log.info("lock");
	}

	@Override
	public void unlock() {
		log.info("unlock");
	}

	@Override
	public void thankyou() {
		log.info("thankyou");
	}

	@Override
	public void alarm() {
		log.info("alarm");
	}
}
