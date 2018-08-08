package de.amr.statemachine;

import java.util.logging.Logger;

/**
 * 
 * @author Armin Reichert
 *
 * @param <S> state identifier type
 * @param <E> event type
 */
public class StateMachineTracer<S, E> {

	private final StateMachine<S, E> sm;
	private final Logger log;

	public StateMachineTracer(StateMachine<S, E> sm, Logger log) {
		this.sm = sm;
		this.log = log;
	}

	public void stateCreated(S stateLabel) {
		log.info(String.format("%s created state '%s'", sm.getDescription(), stateLabel));
	}

	public void ignoredEvent(E event) {
		log.info(String.format("%s in state %s ignored '%s' (no matching transition).", sm.getDescription(),
				sm.currentState(), event));
	}

	public void enteringInitialState(S intialState) {
		log.info(String.format("%s entering initial state '%s'", sm.getDescription(), intialState));
	}

	public void enteringState(S enteredState) {
		if (sm.state(enteredState).getDuration() != StateObject.ENDLESS) {
			float seconds = sm.state(enteredState).getDuration() / sm.fnPulse.getAsInt();
			log.info(String.format("%s entering state '%s' for %.2f seconds (%d frames)", sm.getDescription(),
					enteredState, seconds, sm.state(enteredState).getDuration()));
		} else {
			log.info(String.format("%s entering state '%s'", sm.getDescription(), enteredState));
		}
	}

	public void exitingState(S exitedState) {
		log.info(String.format("%s exiting state '%s'", sm.getDescription(), exitedState));
	}

	public void firingTransition(Transition<S, E> t) {
		if (t.getEvent() == null) {
			if (t.from != t.to) {
				log.info(String.format("%s changing from '%s' to '%s'", sm.getDescription(), t.from, t.to));
			} else {
				log.info(String.format("%s keeps '%s'", sm.getDescription(), t.from));
			}
		} else {
			if (t.from != t.to) {
				log.info(String.format("%s changing from '%s' to '%s' on '%s'", sm.getDescription(), t.from, t.to,
						t.getEvent()));
			} else {
				log.info(String.format("%s keeps '%s' on '%s'", sm.getDescription(), t.from, t.getEvent()));
			}
		}
	}
}
