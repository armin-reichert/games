package de.amr.statemachine;

import java.util.logging.Logger;

public class StateMachineTracer<S, E> {

	private final StateMachine<S, E> sm;
	private Logger log;

	public StateMachineTracer(StateMachine<S, E> sm) {
		this.sm = sm;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	public void stateCreated(S stateLabel) {
		if (log != null) {
			log.info(String.format("FSM(%s) created state '%s'", sm.getDescription(), stateLabel));
		}
	}

	public void ignoredEvent(E event) {
		if (log != null) {
			log.info(String.format("FSM(%s) in state %s ignored '%s' (no matching transition).", sm.getDescription(),
					sm.currentState(), event));
		}
	}

	public void enteringInitialState(S intialState) {
		if (log != null) {
			log.info(String.format("FSM(%s) entering initial state '%s'", sm.getDescription(), intialState));
		}
	}

	public void enteringState(S enteredState) {
		if (log != null) {
			if (sm.state(enteredState).getDuration() != StateObject.UNLIMITED) {
				float seconds = sm.state(enteredState).getDuration() / sm.fnPulse.getAsInt();
				log.info(String.format("FSM(%s) entering state '%s' for %.2f seconds (%d frames)", sm.getDescription(),
						enteredState, seconds, sm.state(enteredState).getDuration()));
			} else {
				log.info(String.format("FSM(%s) entering state '%s'", sm.getDescription(), enteredState));
			}
		}
	}

	public void exitingState(S exitedState) {
		if (log != null) {
			log.info(String.format("FSM(%s) exiting state '%s'", sm.getDescription(), exitedState));
		}
	}

	public void firingTransition(StateMachine<S, E>.Transition t) {
		if (log != null) {
			if (t.event == null) {
				if (t.from != t.to) {
					log.info(String.format("FSM(%s) changing from '%s' to '%s'", sm.getDescription(), t.from, t.to));
				} else {
					log.info(String.format("FSM(%s) keeps '%s'", sm.getDescription(), t.from));
				}
			} else {
				if (t.from != t.to) {
					log.info(
							String.format("FSM(%s) changing from '%s' to '%s' on '%s'", sm.getDescription(), t.from, t.to, t.event));
				} else {
					log.info(String.format("FSM(%s) keeps '%s' on '%s'", sm.getDescription(), t.from, t.event));
				}
			}
		}
	}
}
