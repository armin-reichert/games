package de.amr.easy.fsm;

public interface FSMAction<StateID, Event> {

	public void run(FSMContext<StateID, Event> context);

}
