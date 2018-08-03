package de.amr.statemachine;

import java.util.logging.Logger;

public class StateMachineTracer {

	private final StateMachine<?, ?> sm;
	private Logger log;

	public StateMachineTracer(StateMachine<?, ?> sm) {
		this.sm = sm;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	public void stateCreated(Object stateLabel) {
		if (log != null) {
			log.info(String.format("FSM(%s) created state '%s'", sm.getDescription(), stateLabel));
		}
	}

	public void inputIgnored(Object event) {
		if (log != null) {
			log.info(String.format("FSM(%s) in state %s ignored '%s' (no matching transition).", sm.getDescription(),
					sm.currentStateLabel(), event));
		}
	}

	public void enteringInitialState() {
		if (log != null) {
			log.info(String.format("FSM(%s) entering initial state '%s'", sm.getDescription(), sm.currentStateLabel()));
		}
	}

	public void enteringState() {
		if (log != null) {
			if (sm.state().getDuration() != StateObject.FOREVER) {
				float seconds = sm.state().getDuration() / sm.fnPulse.getAsInt();
				log.info(String.format("FSM(%s) entering state '%s' for %.2f seconds (%d frames)", sm.getDescription(),
						sm.currentStateLabel(), seconds, sm.state().getDuration()));
			} else {
				log.info(String.format("FSM(%s) entering state '%s'", sm.getDescription(), sm.currentStateLabel()));
			}
		}
	}

	public void exitingState() {
		if (log != null) {
			log.info(String.format("FSM(%s) exiting state '%s'", sm.getDescription(), sm.currentStateLabel()));
		}
	}

	public void transition(Object event, Object oldState, Object newState) {
		if (log != null) {
			if (event == null) {
				if (oldState != newState) {
					log.info(String.format("FSM(%s) changing from '%s' to '%s'", sm.getDescription(), oldState, newState));
				} else {
					log.info(String.format("FSM(%s) keeps '%s'", sm.getDescription(), oldState));
				}
			} else {
				if (oldState != newState) {
					log.info(String.format("FSM(%s) changing from '%s' to '%s' on '%s'", sm.getDescription(), oldState,
							newState, event));
				} else {
					log.info(String.format("FSM(%s) keeps '%s' on '%s'", sm.getDescription(), oldState, event));
				}
			}
		}
	}
}
