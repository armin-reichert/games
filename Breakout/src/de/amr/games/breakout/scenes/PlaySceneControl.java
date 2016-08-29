package de.amr.games.breakout.scenes;

import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBat;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBrick;
import static de.amr.games.breakout.scenes.PlayEvent.Tick;
import static de.amr.games.breakout.scenes.PlayState.BallOut;
import static de.amr.games.breakout.scenes.PlayState.Initial;
import static de.amr.games.breakout.scenes.PlayState.Playing;
import static java.awt.event.KeyEvent.VK_SPACE;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.easy.game.Application;
import de.amr.easy.game.input.Key;
import de.amr.games.breakout.entities.Ball;
import de.amr.games.breakout.entities.Brick;

public class PlaySceneControl extends FSM<PlayState, PlayEvent> {

	@Override
	protected Map<PlayState, FSMState<PlayState, PlayEvent>> createStateMap() {
		return new EnumMap<>(PlayState.class);
	}

	public PlaySceneControl(PlayScene scene) {
		final Ball ball = Application.Entities.findAny(Ball.class);
		/*@formatter:off*/
		beginFSM()
			.description("Breakout Application Control")
			.acceptedEvents(EnumSet.allOf(PlayEvent.class))
			.defaultEvent(Tick)
			.initialState(Initial)
			
			.state(Initial)
				.entering(scene::reset)
				.into(Playing).when(() -> Key.pressedOnce(VK_SPACE))
				.keep()
			.end()
			
			.state(Playing)
				.entering(scene::shootBall)
				.keep().act(scene::updateEntities)
				.keep().on(BallHitsBat).act(scene::bounceBallFromBat)
				.keep().on(BallHitsBrick).act((context) -> {
					Brick brick = context.getEvent().collision.getSecond();
					scene.brickWasHit(brick);
				})
				.into(BallOut).when(ball::isOut) 
			.end()
			
			.state(BallOut)
				.entering(() -> {	scene.resetBat();	scene.resetBall(); })
				.into(Playing).when(() -> Key.pressedOnce(VK_SPACE))
				.keep()
			.end()
		.endFSM();
		/*@formatter:on*/
	}
}