package de.amr.easy.fsm;

import java.util.ArrayList;
import java.util.List;

/**
 * State of finite state machine.
 *
 * @param <StateID>
 *          Enum-type for state ID
 * @param <Event>
 *          type for events
 */
public class FSMState<StateID, Event> {

	private final List<FSMTransition<StateID, Event>> transitionList;
	private Runnable entryAction;
	private Runnable exitAction;

	FSMState() {
		transitionList = new ArrayList<>(3);
	}

	public Runnable getEntryAction() {
		return entryAction;
	}

	public void setEntryAction(Runnable entryAction) {
		this.entryAction = entryAction;
	}

	public Runnable getExitAction() {
		return exitAction;
	}

	public void setExitAction(Runnable exitAction) {
		this.exitAction = exitAction;
	}

	public List<FSMTransition<StateID, Event>> getTransitionList() {
		return transitionList;
	}
}
