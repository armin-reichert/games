package de.amr.games.birdy.scenes.start;

import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.GameEvent.Tick;
import static de.amr.games.birdy.Globals.JUMP_KEY;
import static de.amr.games.birdy.Globals.TIME_BEFORE_PLAY;
import static de.amr.games.birdy.scenes.start.StartSceneState.Over;
import static de.amr.games.birdy.scenes.start.StartSceneState.Ready;
import static de.amr.games.birdy.scenes.start.StartSceneState.StartPlaying;
import static de.amr.games.birdy.scenes.start.StartSceneState.Waiting;
import static java.awt.event.KeyEvent.VK_SPACE;

import java.util.EnumMap;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.easy.game.Application;
import de.amr.easy.game.input.Key;
import de.amr.easy.game.timing.Countdown;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.scenes.play.PlayScene;

public class StartSceneControl extends FSM<StartSceneState, GameEvent> {

	private Countdown ready321 = new Countdown(TIME_BEFORE_PLAY);

	@Override
	protected Map<StartSceneState, FSMState<StartSceneState, GameEvent>> createStateMap() {
		return new EnumMap<>(StartSceneState.class);
	}

	public StartSceneControl(StartScene scene) {
		//@formatter:off
		beginFSM()
			.description("Start Scene Control")
			.acceptedEvents(Tick, BirdTouchedGround)
			.defaultEvent(Tick)
			.initialState(Waiting)
			
			.state(Waiting)
				.entering(scene::reset)
				.keep().act(scene::keepBirdInAir)
				.into(Ready).when(() -> Key.down(JUMP_KEY))
				.into(Over).on(BirdTouchedGround)
			.end()
			
			.state(Ready)
				.entering(() -> {	scene.showReadyText(); ready321.restart(); })
				.keep().act(ready321::update)
				.into(Over).on(BirdTouchedGround).act(scene::showTitleText)
				.into(StartPlaying).when(() -> Key.down(JUMP_KEY) || ready321.isComplete())
			.end()
			
			.state(Over)
				.entering(scene::stopScrolling)
				.into(Waiting).when(() -> Key.pressedOnce(VK_SPACE))
				.keep()
			.end()
			
			.state(StartPlaying)
				.entering(() -> Application.Views.show(PlayScene.class))
			.end()
			
		.endFSM();
		//@formatter:on
	}
}