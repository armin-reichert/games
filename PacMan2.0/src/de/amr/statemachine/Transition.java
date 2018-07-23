package de.amr.statemachine;

import java.util.Optional;

/**
 * State transition as seen by client.
 * 
 * @author Armin Reichert
 *
 * @param <StateID>
 *          type for state identification
 * @param <Input>
 *          type for inputs/events
 */
public interface Transition<StateID, Input> {

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

	public Optional<Input> getInput();
}