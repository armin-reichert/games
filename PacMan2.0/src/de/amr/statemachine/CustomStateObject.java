package de.amr.statemachine;

public abstract class CustomStateObject<S, E> extends StateObject<S, E> {

	public CustomStateObject(StateMachine<S, E> sm, S label) {
		super(sm, label);
		entry = this::onEntry;
		exit = this::onExit;
		update = this::onTick;
		defineTransitions();
	}

	public void onEntry(StateObject<S, E> self) {

	}

	public void onExit(StateObject<S, E> self) {

	}

	public void onTick(StateObject<S, E> self) {

	}

	public void defineTransitions() {

	}

}
