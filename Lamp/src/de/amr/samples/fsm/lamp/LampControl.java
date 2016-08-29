package de.amr.samples.fsm.lamp;

import de.amr.easy.fsm.FSM;

public class LampControl extends FSM<Boolean, String> {

	public static final Boolean LAMP_IS_OFF = Boolean.FALSE;
	public static final Boolean LAMP_IS_ON = Boolean.TRUE;

	public static final String SWITCHED = "X";

	public LampControl(Lamp lamp) {
		//@formatter:off
		beginFSM()
			.description("Lamp Controller")
			.acceptedEvents(SWITCHED)
			.initialState(LAMP_IS_OFF)
			.state(LAMP_IS_OFF)
				.entering(lamp::switchOff)
				.into(LAMP_IS_ON).on(SWITCHED)
			.end()
			.state(LAMP_IS_ON)
				.entering(lamp::switchOn)
				.into(LAMP_IS_OFF).on(SWITCHED)
			.end()
		.endFSM();
		//@formatter:on
	}
}