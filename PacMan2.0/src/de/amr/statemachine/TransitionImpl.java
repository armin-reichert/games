package de.amr.statemachine;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Internal implementation of state transition.
 * 
 * @author Armin Reichert
 *
 * @param <StateID>
 *          type for state identification
 * @param <Input>
 *          type for inputs/events
 */
class TransitionImpl<StateID, Input> implements Transition<StateID, Input> {

	final StateMachine<StateID, Input> fsm;
	BooleanSupplier condition;
	Consumer<Transition<StateID, Input>> action;
	StateID from;
	StateID to;
	Input input;

	public TransitionImpl(StateMachine<StateID, Input> fsm) {
		this.fsm = fsm;
	}

	@Override
	public State from() {
		return fsm.state(from);
	}

	@Override
	public State to() {
		return fsm.state(to);
	}

	@Override
	public Optional<Input> getInput() {
		return Optional.ofNullable(input);
	}
}