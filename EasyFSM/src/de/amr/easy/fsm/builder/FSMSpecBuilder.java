package de.amr.easy.fsm.builder;

import java.util.Set;
import java.util.function.BooleanSupplier;

import de.amr.easy.fsm.FSMAction;
import de.amr.easy.fsm.FSMContext;
import de.amr.easy.fsm.FSMSpec;
import de.amr.easy.fsm.FSMTransition;

public class FSMSpecBuilder<StateID, Event> implements FSMStateBuilder<StateID, Event> {

	private final FSMSpec<StateID, Event> fsm;
	private StateID sourceID;
	private FSMTransition<StateID, Event> transition;
	private Event defaultEvent;

	private void assertTransitionCreated() {
		if (transition == null) {
			throw new IllegalStateException("Transition not yet created");
		}
	}

	private void assertStateIDNotNull(StateID stateID) {
		if (stateID == null) {
			throw new IllegalStateException("No state ID specified");
		}
	}

	private void createAndAddTransition(StateID targetID) {
		assertStateIDNotNull(sourceID);
		assertStateIDNotNull(targetID);
		transition = new FSMTransition<StateID, Event>(sourceID, targetID, defaultEvent, null, null);
		if (!fsm.getState(sourceID).getTransitionList().add(transition)) {
			throw new IllegalStateException(
					"Could not add transition: " + transition + " to state " + sourceID);
		}
	}

	public FSMSpecBuilder(FSMSpec<StateID, Event> fsm) {
		this.fsm = fsm;
	}

	public void endFSM() {
		fsm.validate();
	}

	public FSMSpecBuilder<StateID, Event> description(String text) {
		fsm.setDescription(text != null ? text : "Finite State Machine");
		return this;
	}

	public FSMSpecBuilder<StateID, Event> initialState(StateID stateID) {
		assertStateIDNotNull(stateID);
		if (fsm.getInitialState() != null) {
			throw new IllegalStateException("Initial state is already set");
		}
		fsm.setInitialState(stateID);
		fsm.getState(stateID);
		return this;
	}

	@SuppressWarnings("unchecked")
	public FSMSpecBuilder<StateID, Event> acceptedEvents(Event... events) {
		fsm.setAcceptedEvents(events);
		return this;
	}

	public FSMSpecBuilder<StateID, Event> acceptedEvents(Set<Event> events) {
		fsm.setAcceptedEvents(events);
		return this;
	}

	public FSMSpecBuilder<StateID, Event> defaultEvent(Event event) {
		if (defaultEvent == null) {
			defaultEvent = event;
		}
		return this;
	}

	public FSMStateBuilder<StateID, Event> state(StateID stateID) {
		assertStateIDNotNull(stateID);
		fsm.getState(stateID);
		sourceID = stateID;
		return this;
	}

	@Override
	public FSMSpecBuilder<StateID, Event> end() {
		return this;
	}

	@Override
	public FSMSpecBuilder<StateID, Event> entering(Runnable entryAction) {
		assertStateIDNotNull(sourceID);
		if (entryAction == null) {
			throw new IllegalStateException("No entry action specified");
		}
		fsm.getState(sourceID).setEntryAction(entryAction);
		return this;
	}

	@Override
	public FSMSpecBuilder<StateID, Event> exiting(Runnable exitAction) {
		assertStateIDNotNull(sourceID);
		if (exitAction == null) {
			throw new IllegalStateException("No exit action specified");
		}
		fsm.getState(sourceID).setExitAction(exitAction);
		return this;
	}

	@Override
	public FSMSpecBuilder<StateID, Event> keep() {
		createAndAddTransition(sourceID);
		return this;
	}

	@Override
	public FSMSpecBuilder<StateID, Event> into(StateID targetID) {
		createAndAddTransition(targetID);
		return this;
	}

	@Override
	public FSMSpecBuilder<StateID, Event> on(Event event) {
		assertTransitionCreated();
		if (event == null) {
			throw new IllegalStateException("No event specified");
		}
		transition.setEvent(event);
		return this;
	}

	@Override
	public FSMSpecBuilder<StateID, Event> when(BooleanSupplier condition) {
		assertTransitionCreated();
		if (condition == null) {
			throw new IllegalStateException("No condition specified");
		}
		transition.setCondition(condition);
		return this;
	}

	@Override
	public FSMSpecBuilder<StateID, Event> act(FSMAction<StateID, Event> action) {
		assertTransitionCreated();
		if (action == null) {
			throw new IllegalStateException("No action specified");
		}
		transition.setAction(action);
		return this;
	}

	@Override
	public FSMStateBuilder<StateID, Event> act(Runnable action) {
		assertTransitionCreated();
		if (action == null) {
			throw new IllegalStateException("No action specified");
		}
		transition.setAction(new FSMAction<StateID, Event>() {

			@Override
			public void run(FSMContext<StateID, Event> context) {
				action.run();
			}
		});
		return this;
	}
}