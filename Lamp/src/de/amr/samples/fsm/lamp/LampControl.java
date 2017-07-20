package de.amr.samples.fsm.lamp;

import de.amr.easy.statemachine.StateMachine;

public class LampControl extends StateMachine<Boolean, Boolean> {

	// states
	public static final boolean OFF = false;
	public static final boolean ON = true;

	// events
	public static final boolean TOGGLE = true;

	public LampControl(Lamp lamp) {
		super("Lamp Control", Boolean.class, OFF);
		changeOnInput(TOGGLE, OFF, ON, (evt, src, tgt) -> lamp.switchOn());
		changeOnInput(TOGGLE, ON, OFF, (evt, src, tgt) -> lamp.switchOff());
	}

	public void toggle() {
		addInput(TOGGLE);
		update();
	}
}