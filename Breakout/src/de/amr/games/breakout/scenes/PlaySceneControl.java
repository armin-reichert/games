package de.amr.games.breakout.scenes;

import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBat;
import static de.amr.games.breakout.scenes.PlayEvent.BallHitsBrick;
import static de.amr.games.breakout.scenes.PlayState.BallOut;
import static de.amr.games.breakout.scenes.PlayState.Initial;
import static de.amr.games.breakout.scenes.PlayState.Playing;

import java.awt.event.KeyEvent;

import de.amr.easy.game.input.Keyboard;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.breakout.entities.Ball;

public class PlaySceneControl extends StateMachine<PlayState, PlayEvent> {

	public PlaySceneControl(PlayScene scene) {
		super("Breakout Application Control", PlayState.class, Initial);
		final Ball ball = scene.app.entities.findAny(Ball.class);

		state(Initial).entry = s -> scene.reset();
		change(Initial, Playing, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));

		state(Playing).entry = s -> scene.shootBall();
		state(Playing).update = s -> scene.updateEntities();
		changeOnInput(BallHitsBat, Playing, Playing, (s, t) -> scene.bounceBallFromBat());
		changeOnInput(BallHitsBrick, Playing, Playing, (s, t) -> {
			// TODO Brick brick = context.getEvent().collision.getSecond();
			// scene.brickWasHit(brick);
		});
		change(Playing, BallOut, ball::isOut);

		state(BallOut).entry = s -> {
			scene.resetBat();
			scene.resetBall();
		};
		change(BallOut, Playing, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
	}
}