package de.amr.easy.fsm;

public interface FSMEventDispatcher<E> {

	public void dispatch(E event);
}
