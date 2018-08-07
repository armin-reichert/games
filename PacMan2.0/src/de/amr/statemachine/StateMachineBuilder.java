package de.amr.statemachine;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * Builder for state machine instances.
 * 
 * @author Armin Reichert
 *
 * @param <S>
 *          Type for identifying states
 * @param <E>
 *          Type of events
 */
public class StateMachineBuilder<S, E> {

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

	public StateBuilder states() {
		return new StateBuilder();
	}

	public class StateBuilder {
	
		private S state;
		private Runnable entry;
		private Runnable exit;
		private Runnable update;
		private IntSupplier fnDuration;
	
		private void clear() {
			state = null;
			entry = null;
			exit = null;
			update = null;
			fnDuration = () -> StateObject.ENDLESS;
		}
	
		public StateBuilder state(S state) {
			clear();
			this.state = state;
			return this;
		}
	
		public <C extends StateObject<S, E>> StateBuilder impl(C customStateObject) {
			if (customStateObject == null) {
				throw new IllegalArgumentException("Custom state object cannot be NULL");
			}
			customStateObject.machine = sm;
			customStateObject.id = state;
			sm.stateMap.put(state, customStateObject);
			return this;
		}
	
		public StateBuilder duration(IntSupplier fnDuration) {
			if (fnDuration == null) {
				throw new IllegalStateException("Timer function cannot be null for state " + state);
			}
			this.fnDuration = fnDuration;
			return this;
		}
	
		public StateBuilder onEntry(Runnable entry) {
			this.entry = entry;
			return this;
		}
	
		public StateBuilder onExit(Runnable exit) {
			this.exit = exit;
			return this;
		}
	
		public StateBuilder onTick(Runnable update) {
			this.update = update;
			return this;
		}
	
		public StateBuilder build() {
			StateObject<S, E> stateObject;
			if (!sm.stateMap.containsKey(state)) {
				stateObject = new StateObject<>();
				stateObject.machine = sm;
				stateObject.id = state;
				sm.stateMap.put(state, stateObject);
			} else {
				stateObject = sm.stateMap.get(state);
			}
			stateObject.entry = entry;
			stateObject.exit = exit;
			stateObject.update = update;
			stateObject.fnDuration = fnDuration;
			clear();
			return this;
		}
	
		public TransitionBuilder transitions() {
			return new TransitionBuilder();
		}
	}

	public class TransitionBuilder {

		private S from;
		private S to;
		private BooleanSupplier guard;
		private boolean timeout;
		private Class<? extends E> eventType;
		private Consumer<StateTransition<S, E>> action;

		public TransitionBuilder() {
			clear();
		}

		private void clear() {
			from = null;
			to = null;
			guard = null;
			timeout = false;
			eventType = null;
			action = null;
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
			if (guard == null) {
				throw new IllegalArgumentException("Guard cannot be NULL");
			}
			this.guard = guard;
			return this;
		}

		public TransitionBuilder onTimeout() {
			this.timeout = true;
			return this;
		}

		public TransitionBuilder on(Class<? extends E> eventType) {
			if (eventType == null) {
				throw new IllegalArgumentException("Event type of transition cannot be NULL");
			}
			this.eventType = eventType;
			return this;
		}

		public TransitionBuilder act(Consumer<StateTransition<S, E>> action) {
			if (action == null) {
				throw new IllegalArgumentException("Transition action cannot be NULL");
			}
			this.action = action;
			return this;
		}

		public TransitionBuilder build() {
			if (timeout && eventType != null) {
				throw new IllegalStateException("Cannot specify both, onTimeout and on(Event.class)");
			}
			sm.addTransition(from, to, guard, action, eventType, timeout);
			clear();
			return this;
		}

		public StateMachine<S, E> buildStateMachine() {
			sm.description = description;
			sm.initialState = initialState;
			return sm;
		}
	}
}