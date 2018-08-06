package de.amr.statemachine;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StateMachineBuilder<S, E> {

	public static void main(String args[]) {
		/*@formatter:off*/
		StateMachine<String, Integer> sm = StateMachine.builder(String.class, Integer.class)
			.description("SampleFSM")
			.initialState("A")
			.states()
				.state("A")
				.state("B").impl(null)
				.state("C")
			.transitions()
				.change("A", "B").when(() -> 10 > 9).act(t -> {
					System.out.println("Action");
				}).build()
				.keep("B").when(() -> 10 > 9).build()
			.buildStateMachine();
		/*@formatter:on*/
		sm.init();
	}

	private StateMachine<S, E> sm;
	private String description;
	private S initialState;

	public StateMachineBuilder(Class<S> stateLabelType) {
		sm = new StateMachine<>(stateLabelType);
	}
	
	public StateMachineBuilder<S, E> description(String description) {
		this.description = description != null ? description : getClass().getSimpleName();
		return this;
	}

	public StateMachineBuilder<S, E> initialState(S initialState) {
		this.initialState = initialState;
		return this;
	}

	public class TransitionBuilder {

		private S from;
		private S to;
		private BooleanSupplier guard;
		private boolean onTimeout;
		private Class<? extends E> eventType;
		private Consumer<StateTransition<S, E>> action;
		
		public StateObject<S,E> _state(S state) {
			return sm.state(state);
		}

		public TransitionBuilder() {
			clear();
		}

		private void clear() {
			from = to = null;
			guard = () -> true;
			onTimeout = false;
			eventType = null;
			action = t -> {
			};
		}

		public TransitionBuilder change(S from, S to) {
			this.from = from;
			this.to = to;
			return this;
		}

		public TransitionBuilder keep(S state) {
			this.from = this.to = state;
			return this;
		}

		public TransitionBuilder when(BooleanSupplier guard) {
			this.guard = guard;
			return this;
		}

		public TransitionBuilder onTimeout() {
			this.onTimeout = true;
			return this;
		}

		public TransitionBuilder on(Class<? extends E> eventType) {
			this.eventType = eventType;
			return this;
		}

		public TransitionBuilder act(Consumer<StateTransition<S, E>> action) {
			this.action = action;
			return this;
		}

		public TransitionBuilder build() {
			if (onTimeout && eventType != null) {
				throw new IllegalStateException("Cannot specify onTimeout and onEvent for single transition");
			}
			StateMachine<S, E>.Transition t = sm.addTransition(from, to, guard, guard, action, eventType);
			if (onTimeout) {
				t.firingCondition = () -> sm.state(t.from).isTerminated() && t.guard.getAsBoolean();
			} else if (eventType != null) {
				t.firingCondition = () -> sm.hasMatchingEvent(t.eventType) && t.guard.getAsBoolean();
			}
			clear();
			return this;
		}

		public StateMachine<S, E> buildStateMachine() {
			sm.description = description;
			sm.initialState = initialState;
			return sm;
		}
	}

	public class StateBuilder {

		private S state;
		private Consumer<StateObject<S, E>> entry;
		private Consumer<StateObject<S, E>> exit;
		private Consumer<StateObject<S, E>> update;

		private void clear() {
			state = null;
			entry = null;
			exit = null;
			update = null;
		}

		public StateBuilder state(S state) {
			this.state = state;
			return this;
		}

		public <C extends StateObject<S, E>> StateBuilder impl(Supplier<C> customStateConstructor) {
			if (customStateConstructor == null) {
				throw new IllegalArgumentException("Custom state constructor must be specified");
			}
			C customStateObject = customStateConstructor.get();
			customStateObject.sm = sm;
			customStateObject.state = state;
			sm.stateMap.put(state, customStateObject);
			return this;
		}

		public StateBuilder onEntry(Consumer<StateObject<S, E>> entry) {
			this.entry = entry;
			return this;
		}

		public StateBuilder onExit(Consumer<StateObject<S, E>> exit) {
			this.exit = exit;
			return this;
		}

		public StateBuilder onTick(Consumer<StateObject<S, E>> update) {
			this.update = update;
			return this;
		}

		public StateBuilder build() {
			if (!sm.stateMap.containsKey(state)) {
				StateObject<S, E> stateObject = new StateObject<>();
				stateObject.sm = sm;
				stateObject.state = state;
				sm.stateMap.put(state, stateObject);
			}
			sm.state(state).entry = entry;
			sm.state(state).exit = exit;
			sm.state(state).update = update;
			clear();
			return this;
		}

		public TransitionBuilder transitions() {
			return new TransitionBuilder();
		}
	}

	public StateBuilder states() {
		return new StateBuilder();
	}
}