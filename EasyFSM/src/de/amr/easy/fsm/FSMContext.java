package de.amr.easy.fsm;

public interface FSMContext<StateID, Event> {

	public FSMSpec<StateID, Event> getFSM();

	public Event getEvent();

}
