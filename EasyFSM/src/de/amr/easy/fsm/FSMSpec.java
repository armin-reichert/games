package de.amr.easy.fsm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.amr.easy.fsm.builder.FSMSpecBuilder;
import de.amr.easy.fsm.graphviz.FSMGraphVizExporter;

/**
 * Finite State Machine.
 * 
 * @param <StateID>
 *          Type for state IDs
 * @param <Event>
 *          Type for events/input symbols
 */
public abstract class FSMSpec<StateID, Event> {

	private Set<Event> acceptedEvents;
	private Map<StateID, FSMState<StateID, Event>> statesByID;
	private StateID initialStateID;
	private String description;

	public FSMSpecBuilder<StateID, Event> beginFSM() {
		return new FSMSpecBuilder<StateID, Event>(this);
	}

	protected FSMSpec() {
		acceptedEvents = Collections.emptySet();
		statesByID = createStateMap();
		initialStateID = null;
		description = "Finite State Machine";
	}

	public Iterable<StateID> states() {
		return statesByID.keySet();
	}

	public Iterable<FSMTransition<StateID, Event>> transitions(StateID state) {
		return statesByID.get(state).getTransitionList();
	}

	protected Map<StateID, FSMState<StateID, Event>> createStateMap() {
		return new HashMap<>();
	}

	public StateID getInitialState() {
		return initialStateID;
	}

	public void setInitialState(StateID initialStateID) {
		this.initialStateID = initialStateID;
	}

	public void setAcceptedEvents(Set<Event> events) {
		acceptedEvents = events != null ? events : Collections.emptySet();
	}

	@SuppressWarnings("unchecked")
	public void setAcceptedEvents(Event... events) {
		acceptedEvents = events.length > 0 ? new HashSet<>(Arrays.asList(events))
				: Collections.emptySet();
	}

	public boolean isEventAccepted(Event event) {
		return acceptedEvents.contains(event);
	}

	public FSMState<StateID, Event> getState(StateID stateID) {
		if (!statesByID.containsKey(stateID)) {
			statesByID.put(stateID, new FSMState<>());
		}
		return statesByID.get(stateID);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	FSMTransition<StateID, Event> findTransition(FSMState<StateID, Event> state, Event event) {
		for (FSMTransition<StateID, Event> t : state.getTransitionList()) {
			if (t.getEvent() != null && t.getCondition() != null && t.isApplicable(event)) {
				return t;
			}
		}
		for (FSMTransition<StateID, Event> t : state.getTransitionList()) {
			if (t.getEvent() != null && t.isApplicable(event)) {
				return t;
			}
		}
		for (FSMTransition<StateID, Event> t : state.getTransitionList()) {
			if (t.getCondition() != null && t.isApplicable(event)) {
				return t;
			}
		}
		for (FSMTransition<StateID, Event> t : state.getTransitionList()) {
			if (t.isApplicable(event)) {
				return t;
			}
		}
		return null;
	}

	public void validate() {
		if (statesByID == null) {
			throw new IllegalStateException("No states have been created");
		}
		if (initialStateID == null) {
			throw new IllegalStateException("Initial state is NULL! Missing .initialState() call?");
		}
		for (StateID stateID : statesByID.keySet()) {
			for (FSMTransition<StateID, Event> transition : getState(stateID).getTransitionList()) {
				if (transition.getSource() == null) {
					throw new IllegalStateException("Source state is missing in transition: " + transition);
				}
				if (transition.getTarget() == null) {
					throw new IllegalStateException("Target state is missing in transition: " + transition);
				}
				if (transition.getEvent() == null && transition.getCondition() == null) {
					throw new IllegalStateException(
							"Either event or condition must be specified in transition: " + transition);
				}
				if (transition.getEvent() == null && transition.getCondition() != null) {
					throw new IllegalStateException(
							"Event must be specified in conditional transition: " + transition);
				}
			}
		}
	}

	@Override
	public String toString() {
		if (statesByID == null) {
			return super.toString();
		}
		StringBuilder s = new StringBuilder(description);
		s.append(":\n");
		s.append("States: (" + statesByID.size() + ")\n");
		for (StateID state : statesByID.keySet()) {
			s.append(state.toString());
			s.append("\n");
		}
		s.append("Transitions:\n");
		for (FSMState<StateID, Event> state : statesByID.values()) {
			for (FSMTransition<StateID, ?> t : state.getTransitionList()) {
				s.append(t.toString()).append("\n");
			}
		}
		return s.toString();
	}

	public String toGraphViz() {
		return new FSMGraphVizExporter<StateID, Event>().exportFSM(this);
	}
}