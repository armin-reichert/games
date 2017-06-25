package de.amr.games.birdy.scenes.start;

import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.Globals.JUMP_KEY;
import static de.amr.games.birdy.Globals.TIME_BEFORE_PLAY;
import static de.amr.games.birdy.scenes.start.StartSceneState.Over;
import static de.amr.games.birdy.scenes.start.StartSceneState.Ready;
import static de.amr.games.birdy.scenes.start.StartSceneState.StartPlaying;
import static de.amr.games.birdy.scenes.start.StartSceneState.Waiting;
import static java.awt.event.KeyEvent.VK_SPACE;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.scenes.play.PlayScene;

public class StartSceneControl extends StateMachine<StartSceneState, GameEvent> {

	public StartSceneControl(BirdyGame app, StartScene scene) {
		super("Start Scene Control", StartSceneState.class, Waiting);

		state(Waiting).entry = s -> scene.reset();
		state(Waiting).update = s -> scene.keepBirdInAir();
		change(Waiting, Ready, () -> Keyboard.keyDown(JUMP_KEY));
		changeOnInput(BirdTouchedGround, Waiting, Over);

		state(Ready).entry = s -> {
			s.setDuration(TIME_BEFORE_PLAY);
			scene.showReadyText();
		};
		changeOnInput(BirdTouchedGround, Ready, Over, (s, t) -> scene.showTitleText());
		change(Ready, StartPlaying, () -> Keyboard.keyDown(JUMP_KEY));
		changeOnTimeout(Ready, StartPlaying);

		state(Over).entry = s -> scene.stopScrolling();
		change(Over, Waiting, () -> Keyboard.keyPressedOnce(VK_SPACE));

		state(StartSceneState.StartPlaying).entry = s -> app.views.show(PlayScene.class);
	}
}