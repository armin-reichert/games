package de.amr.games.pacman.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.amr.statemachine.StateMachine;
import de.amr.statemachine.StateObject;

public class StateMachineBuilderTest {
	
	interface Event {
	}
	
	class EventX implements Event {
		
	}

	class StateB extends StateObject<String, Event> {
	}

	@Test
	public void test() {
		/*@formatter:off*/
		StateMachine<String, Event> sm = StateMachine.builder(String.class, Event.class)
			.description("SampleFSM")
			.initialState("A")
			.states()
				.state("A")
				.state("B").impl(new StateB())
				.state("C")
			.transitions()
				.change("A", "B").on(EventX.class).act(t -> {
					System.out.println("Action");
				}).build()
				.keep("B").when(() -> 10 > 9).build()
			.endStateMachine();
		/*@formatter:on*/

		sm.init();
		assertTrue(sm.currentState().equals("A"));
		
		sm.enqueue(new EventX());
		sm.update();
		assertTrue(sm.currentState().equals("B"));
	}
}