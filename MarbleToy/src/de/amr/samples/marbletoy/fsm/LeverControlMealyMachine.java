package de.amr.samples.marbletoy.fsm;

import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID.LLL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID.LLR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID.LRL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID.LRR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID.RLL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID.RLR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID.RRL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID.RRR;

import java.util.function.Consumer;

import de.amr.easy.statemachine.StateMachine;
import de.amr.easy.statemachine.Transition;
import de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.StateID;

public class LeverControlMealyMachine extends StateMachine<StateID, Character> {

	public enum StateID {
		LLL, LLR, LRL, LRR, RLL, RLR, RRL, RRR;
	};

	private final StringBuilder output = new StringBuilder();
	private final Consumer<Transition<StateID, Character>> C = t -> output.append('C');
	private final Consumer<Transition<StateID, Character>> D = t -> output.append('D');

	public LeverControlMealyMachine() {
		super("Mealy Machine for Marble MarbleToy", StateID.class, StateID.LLL);
		changeOnInput('A', LLL, RLL, C);
		changeOnInput('B', LLL, LRR, C);
		changeOnInput('A', LLR, RLR, C);
		changeOnInput('B', LLR, LRL, D);
		changeOnInput('A', LRL, RRL, C);
		changeOnInput('B', LRL, LLL, D);
		changeOnInput('A', LRR, RRR, C);
		changeOnInput('B', LRR, LLR, D);
		changeOnInput('A', RLL, LLR, C);
		changeOnInput('B', RLL, RRR, C);
		changeOnInput('A', RLR, LLL, D);
		changeOnInput('B', RLR, RRL, D);
		changeOnInput('A', RRL, LRR, C);
		changeOnInput('B', RRL, RLL, D);
		changeOnInput('A', RRR, LRL, D);
		changeOnInput('B', RRR, RLR, D);
	}

	public boolean accepts(String input) {
		init();
		output.setLength(0);
		for (int i = 0; i < input.length(); ++i) {
			addInput(input.charAt(i));
			update();
		}
		return output.length() > 0 && output.charAt(output.length() - 1) == 'D';
	}
}