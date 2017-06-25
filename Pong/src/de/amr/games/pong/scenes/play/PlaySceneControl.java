package de.amr.games.pong.scenes.play;

import static de.amr.games.pong.PongGlobals.SERVING_TIME;
import static de.amr.games.pong.scenes.play.PlaySceneState.GameOver;
import static de.amr.games.pong.scenes.play.PlaySceneState.Initialized;
import static de.amr.games.pong.scenes.play.PlaySceneState.Playing;
import static de.amr.games.pong.scenes.play.PlaySceneState.Serving;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.statemachine.StateMachine;

public class PlaySceneControl extends StateMachine<PlaySceneState, PlaySceneEvent> {

	public PlaySceneControl(PongPlayScene scene) {
		super("PongControl", PlaySceneState.class, Initialized);

		state(Initialized).entry = s -> {
			scene.resetScores();
		};

		change(Initialized, Serving);

		state(Serving).entry = s -> {
			scene.prepareBall();
			s.setDuration(SERVING_TIME);
		};

		changeOnTimeout(Serving, Playing, (s, t) -> scene.shootBall());

		state(Playing).update = s -> {
			scene.updateEntities();
			if (scene.leftPaddleHitsBall()) {
				scene.bounceBallFromLeftPaddle();
			} else if (scene.rightPaddleHitsBall()) {
				scene.bounceBallFromRightPaddle();
			}
		};

		change(Playing, Serving, () -> scene.isBallOutLeft(), (s, t) -> scene.assignPointToRightPlayer());
		change(Playing, Serving, () -> scene.isBallOutRight(), (s, t) -> scene.assignPointToLeftPlayer());
		change(Playing, GameOver, () -> scene.leftPlayerWins() || scene.rightPlayerWins());

		change(GameOver, Initialized, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
	}
}