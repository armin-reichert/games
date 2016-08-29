package de.amr.easy.fsm.graphviz;

import de.amr.easy.fsm.FSMSpec;
import de.amr.easy.fsm.FSMTransition;

public class FSMGraphVizExporter<StateID, Event> {

	private int fontSize = 8;
	private boolean leftToRight = true;

	public String exportFSM(FSMSpec<StateID, Event> fsm) {
		StringBuilder sb = new StringBuilder();
		sb.append("digraph {\n");
		if (leftToRight) {
			sb.append("rankdir=LR;\n");
		}
		sb.append("node [fontsize=").append(fontSize).append("]\n");
		sb.append("edge [fontsize=").append(fontSize).append("]\n");
		for (StateID state : fsm.states()) {
			for (FSMTransition<StateID, Event> t : fsm.transitions(state)) {
				sb.append(t.getSource()).append(" -> ").append(t.getTarget());
				StringBuilder label = new StringBuilder();
				if (t.getEvent() != null) {
					label.append(t.getEvent());
				}
				if (t.getCondition() != null) {
					label.append("[condition]");
				}
				sb.append("[label=\"").append(label).append("\"]").append("\n");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public void setLeftToRight(boolean leftToRight) {
		this.leftToRight = leftToRight;
	}
}
