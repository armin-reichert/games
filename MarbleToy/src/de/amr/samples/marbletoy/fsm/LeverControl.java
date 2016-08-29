package de.amr.samples.marbletoy.fsm;

import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.LLL;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.LLL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.LLR;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.LLR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.LRL;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.LRL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.LRR;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.LRR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.RLL;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.RLL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.RLR;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.RLR_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.RRL;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.RRL_D;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.RRR;
import static de.amr.samples.marbletoy.fsm.LeverControl.StateID.RRR_D;

import java.util.EnumMap;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.easy.fsm.graphviz.FSMGraphVizExporter;
import de.amr.samples.marbletoy.entities.MarbleToy;
import de.amr.samples.marbletoy.fsm.LeverControl.StateID;

public class LeverControl extends FSM<StateID, Character> {

	public enum StateID {
		LLL, LLR, LRL, LRR, RLL, RLR, RRL, RRR, LLL_D, LLR_D, LRL_D, LRR_D, RLL_D, RLR_D, RRL_D, RRR_D;
	};

	@Override
	protected Map<StateID, FSMState<StateID, Character>> createStateMap() {
		return new EnumMap<>(StateID.class);
	}

	public LeverControl(MarbleToy toy) {
		/*@formatter:off*/
		beginFSM()
			.description("Marble Toy Lever Control")
			.acceptedEvents('A', 'B')
			.initialState(LLL)
			.state(LLL).into(RLL).on('A').end()
			.state(LLL).into(LRR).on('B').end()
			.state(LLL_D).into(RLL).on('A').end()
			.state(LLL_D).into(LRR).on('B').end()
			.state(LLR).into(RLR).on('A').end()
			.state(LLR).into(LRL_D).on('B').end()
			.state(LLR_D).into(RLR).on('A').end()
			.state(LLR_D).into(LRL_D).on('B').end()
			.state(LRL).into(RRL).on('A').end()
			.state(LRL).into(LLL_D).on('B').end()
			.state(LRL_D).into(RRL).on('A').end()
			.state(LRL_D).into(LLL_D).on('B').end()
			.state(LRR).into(RRR).on('A').end()
			.state(LRR).into(LLR_D).on('B').end()
			.state(LRR_D).into(RRR).on('A').end()
			.state(LRR_D).into(LLR_D).on('B').end()
			.state(RLL).into(LLR).on('A').end()
			.state(RLL).into(RRR).on('B').end()
			.state(RLL_D).into(LLR).on('A').end()
			.state(RLL_D).into(RRR).on('B').end()
			.state(RLR).into(LLL_D).on('A').end()
			.state(RLR).into(RRL_D).on('B').end()
			.state(RLR_D).into(LLL_D).on('A').end()
			.state(RLR_D).into(RRL_D).on('B').end()
			.state(RRL).into(LRR).on('A').end()
			.state(RRL).into(RLL_D).on('B').end()
			.state(RRL_D).into(LRR).on('A').end()
			.state(RRL_D).into(RLL_D).on('B').end()
			.state(RRR).into(LRL_D).on('A').end()
			.state(RRR).into(RLR_D).on('B').end()
			.state(RRR_D).into(LRL_D).on('A').end()
			.state(RRR_D).into(RLR_D).on('B').end()
		.endFSM();
		/*@formatter:on*/

		for (StateID stateID : StateID.values()) {
			getState(stateID).setEntryAction(toy::updateLevers);
		}
	}

	public boolean isFinalState() {
		return getCurrentState().name().endsWith("_D");
	}

	public boolean isRoutingLeft(int leverIndex) {
		return getCurrentState().name().charAt(leverIndex) == 'L';
	}

	public boolean accepts(String input) {
		init();
		for (int i = 0; i < input.length(); ++i) {
			run(input.charAt(i));
		}
		return isFinalState();
	}

	@Override
	public String toGraphViz() {
		FSMGraphVizExporter<StateID, Character> gv = new FSMGraphVizExporter<>();
		gv.setFontSize(10);
		gv.setLeftToRight(true);
		return gv.exportFSM(this);
	}

}