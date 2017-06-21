package de.amr.games.pacman.core.statemachine;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

class Transition<StateID> {

	BooleanSupplier condition;
	BiConsumer<State, State> action;
	StateID oldState;
	StateID newState;
}
