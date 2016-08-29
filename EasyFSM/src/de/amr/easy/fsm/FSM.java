package de.amr.easy.fsm;

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

public abstract class FSM<StateID, Event> extends FSMSpec<StateID, Event> {

	public static final Logger LOG = Logger.getLogger(FSMSpec.class.getName());

	private final Deque<Event> eventQ = new LinkedList<>();
	private StateID currentStateID;

	protected FSM() {
		currentStateID = null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append("\nCurrent state: ").append(currentStateID).append("\nEvent queue: ");
		for (Event e : eventQ) {
			sb.append(e.toString()).append(" ");
		}
		return sb.toString();
	}

	public StateID getCurrentState() {
		return currentStateID;
	}

	public void enqueue(Event event) {
		if (event == null) {
			throw new IllegalArgumentException("Cannot enqueue null event");
		}
		if (!isEventAccepted(event)) {
			throw new IllegalArgumentException("Cannot enqueue unaccepted event '" + event + "'");
		}
		eventQ.addLast(event);
	}

	public void run(Event event) {
		enqueue(event);
		run();
	}

	public void run() {
		while (!eventQ.isEmpty()) {
			process(eventQ.pollFirst());
		}
	}

	public void init() {
		eventQ.clear();
		if (getInitialState() == null) {
			throw new IllegalStateException("Initial state not defined");
		}
		currentStateID = getInitialState();
		doEnterState(currentStateID);
		LOG.info(getDescription() + ": Entered initial state " + currentStateID);
	}

	private void process(Event event) {
		if (currentStateID == null) {
			throw new IllegalStateException("Finite state machine not initialized. Missing init() call?");
		}
		final FSMTransition<StateID, Event> transition = findTransition(getState(currentStateID),
				event);
		if (transition == null) {
			throw new IllegalStateException(getDescription() + ": No transition defined for event '"
					+ event + "' in state " + currentStateID);
		}
		if (transition.getSource() != transition.getTarget()) {
			doExitState(currentStateID);
			LOG.info(getDescription() + ": " + transition.toString());
		}
		if (transition.getAction() != null) {
			transition.getAction().run(new FSMContext<StateID, Event>() {

				@Override
				public FSMSpec<StateID, Event> getFSM() {
					return FSM.this;
				}

				@Override
				public Event getEvent() {
					return transition.getEvent();
				}
			});
		}
		currentStateID = transition.getTarget();
		if (transition.getSource() != transition.getTarget()) {
			doEnterState(transition.getTarget());
		}
	}

	private void doExitState(StateID stateID) {
		Runnable exitAction = getState(currentStateID).getExitAction();
		if (exitAction != null) {
			exitAction.run();
		}
	}

	private void doEnterState(StateID stateID) {
		Runnable entryAction = getState(stateID).getEntryAction();
		if (entryAction != null) {
			entryAction.run();
		}
	}
}
