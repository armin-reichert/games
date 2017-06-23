package de.amr.games.pacman.core.statemachine;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

/**
 * Transition in a state machine.
 * 
 * @author Armin Reichert
 *
 * @param <StateID>
 *          type for identifying states
 */
class Transition<StateID> {

	BooleanSupplier condition;
	BiConsumer<State, State> action;
	StateID oldState;
	StateID newState;
}