package de.amr.games.pong.scenes.play;

import static de.amr.games.pong.Globals.NEW_BALL_DELAY;
import static de.amr.games.pong.scenes.play.PlaySceneEvent.Tick;
import static de.amr.games.pong.scenes.play.PlaySceneState.GameOver;
import static de.amr.games.pong.scenes.play.PlaySceneState.Playing;
import static de.amr.games.pong.scenes.play.PlaySceneState.Started;
import static de.amr.games.pong.scenes.play.PlaySceneState.WaitingForBall;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.easy.game.timing.Countdown;

public class PlaySceneControl extends FSM<PlaySceneState, PlaySceneEvent> {

	private Countdown waitForBall321 = new Countdown(NEW_BALL_DELAY);

	@Override
	protected Map<PlaySceneState, FSMState<PlaySceneState, PlaySceneEvent>> createStateMap() {
		return new EnumMap<>(PlaySceneState.class);
	}

	public PlaySceneControl(PlayScene scene) {
		/*@formatter:off */
		beginFSM()
			.acceptedEvents(EnumSet.allOf(PlaySceneEvent.class))
			.defaultEvent(Tick)
			.initialState(Started)
			.state(Started)
				.entering(scene::resetScores)
				.into(WaitingForBall)
			.end()
			.state(WaitingForBall)
				.entering(() -> {
					scene.prepareBall();
					waitForBall321.restart();
				})
				.keep().act(waitForBall321::update)
				.into(Playing).when(waitForBall321::isComplete).act(scene::shootBall)
			.end()
			.state(Playing)
				.keep().act(scene::updateEntities)
				.keep().when(scene::leftPaddleHitsBall).act(scene::bounceBallFromLeftPaddle)
				.keep().when(scene::rightPaddleHitsBall).act(scene::bounceBallFromRightPaddle)
				.into(WaitingForBall).when(scene::isBallOutLeft).act(scene::assignPointToRightPlayer)
				.into(WaitingForBall).when(scene::isBallOutRight).act(scene::assignPointToLeftPlayer)
				.into(GameOver).when(scene::leftPlayerWins)
				.into(GameOver).when(scene::rightPlayerWins)
			.end()
			.state(GameOver).keep().end()
		.endFSM();
		/* @formatter:on */
	}
}
