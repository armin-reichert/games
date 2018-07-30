package de.amr.statemachine;

import java.util.Optional;

/**
 * State transition as seen by client.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          type of state identifiers
 * @param <E>
 *          type of inputs (events)
 */
public interface StateTransition<S, E> {

	/**
	 * The state which is changed by this transition.
	 * 
	 * @return state object
	 */
	public State from();

	/**
	 * The state where this transition leads to.
	 * 
	 * @return state object
	 */
	public State to();

	/**
	 * The input/event which triggered the execution of this transition.
	 * 
	 * @return optional input which triggered transition
	 */
	public Optional<E> event();
}