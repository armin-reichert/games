package de.amr.statemachine;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StateMachineBuilder<S, E> {

	public static void main(String args[]) {
		/*@formatter:off*/
		StateMachine<String, Integer> sm = new StateMachineBuilder<String, Integer>("FSM", String.class, "A")
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

	public StateMachineBuilder(String description, Class<S> stateLabelType, S initialState) {
		sm = new StateMachine<>(description, stateLabelType, initialState);
	}

	public StateMachineBuilder(StateMachine<S, E> sm) {
		this.sm = sm;
	}

	public class TransitionBuilder {

		private S from;
		private S to;
		private BooleanSupplier guard;
		private boolean onTimeout;
		private Class<? extends E> eventType;
		private Consumer<StateTransition<S, E>> action;

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
				t.firingCondition  = () -> sm.state(t.from).isTerminated() && t.guard.getAsBoolean();
			} else if (eventType != null) {
				t.firingCondition  = () -> sm.hasMatchingEvent(t.eventType) && t.guard.getAsBoolean();
			}
			clear();
			return this;
		}

		public StateMachine<S, E> buildStateMachine() {
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
			sm.stateMap.put(state, customStateConstructor.get());
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
				sm.stateMap.put(state, sm.state(state));
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