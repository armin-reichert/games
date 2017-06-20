package de.amr.games.pacman.core.statemachine;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class Transition<StateID> {

	BooleanSupplier condition;
	Consumer<State> action;
	StateID oldState;
	StateID newState;
}
