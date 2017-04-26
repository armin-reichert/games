package de.amr.games.birdy.scenes.play;

import static de.amr.games.birdy.BirdyGame.Game;
import static de.amr.games.birdy.GameEvent.BirdCrashed;
import static de.amr.games.birdy.GameEvent.BirdLeftPassage;
import static de.amr.games.birdy.GameEvent.BirdLeftWorld;
import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.GameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.GameEvent.Tick;
import static de.amr.games.birdy.Globals.OBSTACLE_PIPE_WIDTH;
import static de.amr.games.birdy.assets.BirdySound.BIRD_GETS_POINT;
import static de.amr.games.birdy.assets.BirdySound.BIRD_HITS_OBSTACLE;
import static de.amr.games.birdy.assets.BirdySound.PLAYING_MUSIC;
import static de.amr.games.birdy.scenes.play.PlaySceneState.GameOver;
import static de.amr.games.birdy.scenes.play.PlaySceneState.Playing;
import static de.amr.games.birdy.scenes.play.PlaySceneState.StartingNewGame;

import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import de.amr.easy.fsm.FSM;
import de.amr.easy.fsm.FSMState;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.input.Key;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.start.StartScene;

public class PlaySceneControl extends FSM<PlaySceneState, GameEvent> {

	@Override
	protected Map<PlaySceneState, FSMState<PlaySceneState, GameEvent>> createStateMap() {
		return new EnumMap<>(PlaySceneState.class);
	}

	public PlaySceneControl(PlayScene scene) {
		final Bird bird = Game.entities.findAny(Bird.class);
		final Score score = scene.getApp().score;
		//@formatter:off
		beginFSM()
			.description("Play Scene")
			.acceptedEvents(EnumSet.allOf(GameEvent.class))
			.defaultEvent(Tick)
			.initialState(Playing)
			
			.state(Playing)
				.entering(() -> {
					score.reset();	
					scene.startScrolling();
					PLAYING_MUSIC.loop();
				})
				.keep()
				.keep().on(BirdTouchedPipe).when(() -> score.points > 3)
					.act(() -> {
						BIRD_HITS_OBSTACLE.play();
						score.points -= 3; 
						bird.tr.setX(bird.tr.getX() + OBSTACLE_PIPE_WIDTH + bird.getWidth());
						bird.dispatch(BirdTouchedPipe);
					})
				.into(GameOver).on(BirdTouchedPipe).when(() -> score.points <= 3)
					.act(() -> {
						BIRD_HITS_OBSTACLE.play();
						bird.dispatch(BirdCrashed);
					})
				.keep().on(BirdLeftPassage)
					.act(() -> {
						BIRD_GETS_POINT.play();
						score.points++;
					})
				.into(GameOver).on(BirdTouchedGround)
					.act(() -> {
						PLAYING_MUSIC.stop();
						bird.dispatch(BirdTouchedGround);
					})
				.into(GameOver).on(BirdLeftWorld)
					.act(() -> {
						PLAYING_MUSIC.stop();
						bird.dispatch(BirdLeftWorld);
					})
			.end()		
			
			.state(GameOver)
				.entering(() -> {
					scene.stopScrolling();
				})
				.into(StartingNewGame).when(() -> Key.pressedOnce(KeyEvent.VK_SPACE))
				.keep().on(BirdTouchedGround).act(() -> PLAYING_MUSIC.stop())
				.keep().on(BirdTouchedPipe)
				.keep().on(BirdLeftPassage)
				.keep().on(BirdLeftWorld)
				.keep()
			.end()
			
			.state(StartingNewGame)
				.entering(() -> Game.views.show(StartScene.class))
			.end()
				
		.endFSM();
		//@formatter:on
	}
}
