package de.amr.games.birdy.assets;

import static de.amr.games.birdy.BirdyGame.Game;

import de.amr.easy.game.assets.Sound;

public class BirdySound {

	public static final Sound PLAYING_MUSIC = Game.assets.sound("music/bgmusic.mp3");
	public static final Sound BIRD_DIES = Game.assets.sound("sfx/die.mp3");
	public static final Sound BIRD_HITS_OBSTACLE = Game.assets.sound("sfx/hit.mp3");
	public static final Sound BIRD_GETS_POINT = Game.assets.sound("sfx/point.mp3");
	public static final Sound BIRD_SWOOSHING = Game.assets.sound("sfx/swooshing.mp3");
	public static final Sound BIRD_WING = Game.assets.sound("sfx/wing.mp3");
}
