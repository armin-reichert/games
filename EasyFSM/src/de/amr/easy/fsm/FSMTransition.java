package de.amr.easy.fsm;

import java.util.function.BooleanSupplier;

/**
 * Transition of finite state machine.
 *
 * @param <StateID>
 *          type for state ID
 * 
 * @param <Event>
 *          type for events
 */
public class FSMTransition<StateID, Event> {

	private StateID sourceID;
	private StateID targetID;
	private Event event;
	private BooleanSupplier condition;
	private FSMAction<StateID, Event> action;

	public FSMTransition(StateID source, StateID target, Event event, BooleanSupplier condition,
			FSMAction<StateID, Event> action) {
		this.sourceID = source;
		this.targetID = target;
		this.setEvent(event);
		this.setCondition(condition);
		this.setAction(action);
	}

	public boolean isApplicable(Event e) {
		if (event == null && condition == null) {
			throw new IllegalStateException("Either event or condition must be specified");
		}
		if (event != null && condition != null) {
			return event.equals(e) && condition.getAsBoolean();
		}
		if (event != null && condition == null) {
			return event.equals(e);
		}
		// event == null, condition != null
		return condition.getAsBoolean();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(sourceID).append(")").append(" -> (").append(targetID).append(")");
		sb.append(" {");
		if (event != null) {
			sb.append(event).append("!");
		}
		if (condition != null) {
			sb.append("[condition]");
		}
		if (getAction() != null) {
			sb.append("/action");
		}
		sb.append("}");
		return sb.toString();
	}

	public StateID getSource() {
		return sourceID;
	}

	public void setSource(StateID source) {
		this.sourceID = source;
	}

	public StateID getTarget() {
		return targetID;
	}

	public void setTarget(StateID target) {
		this.targetID = target;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public BooleanSupplier getCondition() {
		return condition;
	}

	public void setCondition(BooleanSupplier condition) {
		this.condition = condition;
	}

	public FSMAction<StateID, Event> getAction() {
		return action;
	}

	public void setAction(FSMAction<StateID, Event> action) {
		this.action = action;
	}
}