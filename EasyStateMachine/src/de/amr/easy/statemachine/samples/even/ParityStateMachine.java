package de.amr.easy.statemachine.samples.even;

import static de.amr.easy.statemachine.samples.even.ParityStateMachine.Parity.EVEN;
import static de.amr.easy.statemachine.samples.even.ParityStateMachine.Parity.ODD;

import de.amr.easy.statemachine.StateMachine;
import de.amr.easy.statemachine.samples.even.ParityStateMachine.Parity;

public class ParityStateMachine extends StateMachine<Parity, Character> {

	public enum Parity {
		EVEN, ODD
	}

	public ParityStateMachine() {
		super("EvenLengthFSM", Parity.class, Parity.EVEN);
		changeOnInput('a', EVEN, ODD);
		changeOnInput('a', ODD, EVEN);
	}

	public void process(String s) {
		init();
		s.chars().forEach(c -> {
			addInput((char) c);
			update();
		});
		System.out.println(String.format("'%s' = %s", s, stateID()));
	}

	public static void main(String[] args) {
		ParityStateMachine fsm = new ParityStateMachine();
		// fsm.setLogger(Logger.getGlobal());
		fsm.process("");
		fsm.process("a");
		fsm.process("ab");
		fsm.process("aba");
		fsm.process("abaa");
		fsm.process("abaaa");
		fsm.process("abbbba");
		fsm.process("abababa");
	}
}