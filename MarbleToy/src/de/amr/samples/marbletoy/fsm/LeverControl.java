package de.amr.samples.marbletoy.fsm;

import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LLL;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LLL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LLR;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LLR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LRL;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LRL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LRR;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.LRR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RLL;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RLL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RLR;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RLR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RRL;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RRL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RRR;
import static de.amr.samples.marbletoy.fsm.LeverControl.ToyState.RRR_D;

import de.amr.easy.statemachine.StateMachine;
import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.samples.marbletoy.fsm.LeverControl.ToyState;

public class LeverControl extends StateMachine<ToyState, Character> {

	public enum ToyState {
		LLL, LLR, LRL, LRR, RLL, RLR, RRL, RRR, LLL_D, LLR_D, LRL_D, LRR_D, RLL_D, RLR_D, RRL_D, RRR_D;
	};

	public LeverControl(MarbleToy toy) {
		super("Marble Toy Lever Control", ToyState.class, LLL);

		for (ToyState stateID : ToyState.values()) {
			state(stateID).entry = state -> updateLevers(toy);
		}

		changeOnInput('A', LLL, RLL);
		changeOnInput('B', LLL, LRR);
		changeOnInput('A', LLL_D, RLL);
		changeOnInput('B', LLL_D, LRR);
		changeOnInput('A', LLR, RLR);
		changeOnInput('B', LLR, LRL_D);
		changeOnInput('A', LLR_D, RLR);
		changeOnInput('B', LLR_D, LRL_D);
		changeOnInput('A', LRL, RRL);
		changeOnInput('B', LRL, LLL_D);
		changeOnInput('A', LRL_D, RRL);
		changeOnInput('B', LRL_D, LLL_D);
		changeOnInput('A', LRR, RRR);
		changeOnInput('B', LRR, LLR_D);
		changeOnInput('A', LRR_D, RRR);
		changeOnInput('B', LRR_D, LLR_D);
		changeOnInput('A', RLL, LLR);
		changeOnInput('B', RLL, RRR);
		changeOnInput('A', RLL_D, LLR);
		changeOnInput('B', RLL_D, RRR);
		changeOnInput('A', RLR, LLL_D);
		changeOnInput('B', RLR, RRL_D);
		changeOnInput('A', RLR_D, LLL_D);
		changeOnInput('B', RLR_D, RRL_D);
		changeOnInput('A', RRL, LRR);
		changeOnInput('B', RRL, RLL_D);
		changeOnInput('A', RRL_D, LRR);
		changeOnInput('B', RRL_D, RLL_D);
		changeOnInput('A', RRR, LRL_D);
		changeOnInput('B', RRR, RLR_D);
		changeOnInput('A', RRR_D, LRL_D);
		changeOnInput('B', RRR_D, RLR_D);
	}

	public boolean isFinalState() {
		return stateID().name().endsWith("_D");
	}

	void updateLevers(MarbleToy toy) {
		for (int i = 0; i < toy.levers.length; ++i) {
			toy.levers[i].setPointsLeft(isRoutingLeft(i));
		}
	}

	public boolean isRoutingLeft(int leverIndex) {
		return stateID().name().charAt(leverIndex) == 'L';
	}

	public boolean process(String input) {
		init();
		input.chars().forEach(ch -> {
			addInput((char) ch);
			update();
		});
		return isFinalState();
	}
}