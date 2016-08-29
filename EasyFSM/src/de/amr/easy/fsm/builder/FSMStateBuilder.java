package de.amr.easy.fsm.builder;

import java.util.function.BooleanSupplier;

import de.amr.easy.fsm.FSMAction;

public interface FSMStateBuilder<StateID, Event> {

	public FSMStateBuilder<StateID, Event> entering(Runnable entryAction);

	public FSMStateBuilder<StateID, Event> exiting(Runnable exitAction);

	public FSMStateBuilder<StateID, Event> keep();

	public FSMStateBuilder<StateID, Event> into(StateID targetID);

	public FSMStateBuilder<StateID, Event> on(Event event);

	public FSMStateBuilder<StateID, Event> when(BooleanSupplier condition);

	public FSMStateBuilder<StateID, Event> act(FSMAction<StateID, Event> action);

	public FSMStateBuilder<StateID, Event> act(Runnable action);

	public FSMSpecBuilder<StateID, Event> end();
}
