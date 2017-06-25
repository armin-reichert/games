package de.amr.games.birdy.scenes.play;

import static de.amr.games.birdy.BirdyGame.Game;
import static de.amr.games.birdy.GameEvent.BirdCrashed;
import static de.amr.games.birdy.GameEvent.BirdLeftPassage;
import static de.amr.games.birdy.GameEvent.BirdLeftWorld;
import static de.amr.games.birdy.GameEvent.BirdTouchedGround;
import static de.amr.games.birdy.GameEvent.BirdTouchedPipe;
import static de.amr.games.birdy.scenes.play.PlaySceneState.GameOver;
import static de.amr.games.birdy.scenes.play.PlaySceneState.Playing;
import static de.amr.games.birdy.scenes.play.PlaySceneState.StartingNewGame;

import java.awt.event.KeyEvent;

import de.amr.easy.game.common.Score;
import de.amr.easy.game.input.Keyboard;
import de.amr.easy.statemachine.StateMachine;
import de.amr.games.birdy.BirdyGame;
import de.amr.games.birdy.GameEvent;
import de.amr.games.birdy.Globals;
import de.amr.games.birdy.assets.BirdySound;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.start.StartScene;

public class PlaySceneControl extends StateMachine<PlaySceneState, GameEvent> {

	public PlaySceneControl(BirdyGame app, PlayScene scene) {
		super("Play Scene Control", PlaySceneState.class, Playing);
		
		final Bird bird = Game.entities.findAny(Bird.class);
		final Score score = scene.getApp().score;

		state(Playing).entry = s -> {
			score.reset();	
			scene.startScrolling();
			BirdySound.PLAYING_MUSIC.loop();
		};
		
		changeOnInput(BirdTouchedPipe, Playing, Playing, () -> score.points > 3, (s,t) -> {
			BirdySound.BIRD_HITS_OBSTACLE.play();
			score.points -= 3; 
			bird.tr.setX(bird.tr.getX() + Globals.OBSTACLE_PIPE_WIDTH + bird.getWidth());
			bird.dispatch(BirdTouchedPipe);
		});
		
		changeOnInput(BirdTouchedPipe, Playing, GameOver, () -> score.points <= 3, (s,t) -> {
			BirdySound.BIRD_HITS_OBSTACLE.play();
			bird.dispatch(BirdCrashed);
		});

		changeOnInput(BirdLeftPassage, Playing, Playing, (s,t) -> {
			BirdySound.BIRD_GETS_POINT.play();
			score.points++;
		});

		changeOnInput(BirdTouchedGround, Playing, GameOver, (s,t) -> {
			BirdySound.PLAYING_MUSIC.stop();
			bird.dispatch(BirdTouchedGround);
		});

		changeOnInput(BirdLeftWorld, Playing, GameOver, (s,t) -> {
			BirdySound.PLAYING_MUSIC.stop();
			bird.dispatch(BirdLeftWorld);
		});
		
		state(GameOver).entry = s -> scene.stopScrolling();
		
		change(GameOver, StartingNewGame, () -> Keyboard.keyPressedOnce(KeyEvent.VK_SPACE));
		changeOnInput(BirdTouchedGround, GameOver, GameOver, (s,t) -> BirdySound.PLAYING_MUSIC.stop());
			
		state(StartingNewGame).entry = s -> app.views.show(StartScene.class);
	}
}
