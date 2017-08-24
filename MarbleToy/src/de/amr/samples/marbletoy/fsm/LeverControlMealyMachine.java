package de.amr.samples.marbletoy.fsm;

import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.LLL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.LLR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.LRL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.LRR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.RLL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.RLR;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.RRL;
import static de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState.RRR;

import java.util.function.Consumer;

import de.amr.easy.statemachine.StateMachine;
import de.amr.easy.statemachine.Transition;
import de.amr.samples.marbletoy.fsm.LeverControlMealyMachine.ToyState;

public class LeverControlMealyMachine extends StateMachine<ToyState, Character> {

	public enum ToyState {
		LLL, LLR, LRL, LRR, RLL, RLR, RRL, RRR;
	};

	private final StringBuilder output = new StringBuilder();
	private final Consumer<Transition<ToyState, Character>> C = t -> output.append('C');
	private final Consumer<Transition<ToyState, Character>> D = t -> output.append('D');

	public LeverControlMealyMachine() {
		super("Mealy Machine for Marble-Toy", ToyState.class, ToyState.LLL);
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

	public boolean process(String input) {
		init();
		output.setLength(0);
		input.chars().forEach(ch -> {
			addInput((char) ch);
			update();
		});
		return output.length() > 0 && output.charAt(output.length() - 1) == 'D';
	}
}