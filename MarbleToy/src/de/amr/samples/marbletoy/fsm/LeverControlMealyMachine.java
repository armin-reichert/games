package de.amr.samples.marbletoy.fsm;

import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State.LLL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State.LLR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State.LRL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State.LRR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State.RLL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State.RLR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State.RRL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State.RRR;

import java.util.EnumMap;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.State;

public class LeverControlMealyMachine extends FSM<State, Character> {

	public enum State {
		LLL, LLR, LRL, LRR, RLL, RLR, RRL, RRR;
	};

	private final StringBuilder output = new StringBuilder();
	private final Runnable C = () -> output.append('C');
	private final Runnable D = () -> output.append('D');

	public LeverControlMealyMachine() {
		/*@formatter:off*/
		beginFSM()
			.description("Mealy Machine for Marble MarbleToy")
			.acceptedEvents('A', 'B')
			.initialState(LLL)
			.state(LLL).into(RLL).on('A').act(C).end()
			.state(LLL).into(LRR).on('B').act(C).end()
			.state(LLR).into(RLR).on('A').act(C).end()
			.state(LLR).into(LRL).on('B').act(D).end()
			.state(LRL).into(RRL).on('A').act(C).end()
			.state(LRL).into(LLL).on('B').act(D).end()
			.state(LRR).into(RRR).on('A').act(C).end()
			.state(LRR).into(LLR).on('B').act(D).end()
			.state(RLL).into(LLR).on('A').act(C).end()
			.state(RLL).into(RRR).on('B').act(C).end()
			.state(RLR).into(LLL).on('A').act(D).end()
			.state(RLR).into(RRL).on('B').act(D).end()
			.state(RRL).into(LRR).on('A').act(C).end()
			.state(RRL).into(RLL).on('B').act(D).end()
			.state(RRR).into(LRL).on('A').act(D).end()
			.state(RRR).into(RLR).on('B').act(D).end()
		.endFSM();		
		/*@formatter:on*/
	}

	@Override
	protected Map<State, FSMState<State, Character>> createStateMap() {
		return new EnumMap<>(State.class);
	}

	public boolean accepts(String input) {
		init();
		output.setLength(0);
		for (int i = 0; i < input.length(); ++i) {
			run(input.charAt(i));
		}
		return output.length() > 0 && output.charAt(output.length() - 1) == 'D';
	}
}