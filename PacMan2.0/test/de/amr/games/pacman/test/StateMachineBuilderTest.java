package de.amr.games.pacman.test;

import org.junit.Test;

import de.amr.statemachine.StateMachine;

public class StateMachineBuilderTest {

	@Test
	public void test() {
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
		
		//TODO add assertions
	}
}