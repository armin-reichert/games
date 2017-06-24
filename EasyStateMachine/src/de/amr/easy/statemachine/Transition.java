package de.amr.easy.statemachine;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

/**
 * A transition in a state machine.
 * 
 * @author Armin Reichert
 *
 * @param <StateID>
 *          type for identifying states
 */
class Transition<StateID> {

	public BooleanSupplier condition;
	public BiConsumer<State, State> action;
	public StateID from;
	public StateID to;
}