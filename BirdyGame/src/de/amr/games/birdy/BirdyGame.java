package de.amr.games.birdy;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Sound;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.play.PlayScene;
import de.amr.games.birdy.scenes.start.StartScene;
import de.amr.games.birdy.scenes.util.SpriteBrowser;

public class BirdyGame extends Application {

	public static void main(String[] args) {
		BirdyGame game = new BirdyGame();
		game.settings.title = "Zwick, das listige Vögelchen";
		game.settings.width = 640;
		game.settings.height = 480;
		game.settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
		launch(game);
	}

	public static final int JUMP_KEY = KeyEvent.VK_UP;
	public static final float WORLD_GRAVITY = 0.3f;
	public static final float WORLD_SPEED = -2.5f;
	public static final int READY_TIME = 120;
	public static final int CITY_MAX_STARS = 5;
	public static final float BIRD_JUMP_SPEED = WORLD_GRAVITY * 2.5f;
	public static final int BIRD_FLAP_DURATION_MILLIS = 50;
	public static final int BIRD_INJURED_TIME = 60;
	public static final int OBSTACLE_MIN_CREATION_TIME = 60;
	public static final int OBSTACLE_MAX_CREATION_TIME = 90;
	public static final int OBSTACLE_PIPE_HEIGHT = 320;
	public static final int OBSTACLE_PIPE_WIDTH = 52;
	public static final int OBSTACLE_MIN_PIPE_HEIGHT = 100;
	public static final int OBSTACLE_PASSAGE_HEIGHT = 100;

	public final Sound PLAYING_MUSIC = assets.sound("music/bgmusic.mp3");
	public final Sound BIRD_DIES = assets.sound("sfx/die.mp3");
	public final Sound BIRD_HITS_OBSTACLE = assets.sound("sfx/hit.mp3");
	public final Sound BIRD_GETS_POINT = assets.sound("sfx/point.mp3");
	public final Sound BIRD_SWOOSHING = assets.sound("sfx/swooshing.mp3");
	public final Sound BIRD_WING = assets.sound("sfx/wing.mp3");
	public final Score score = new Score();

	@Override
	public void init() {
		readSpriteSheet();
		PLAYING_MUSIC.volume(-20);

		entities.add(new Bird(this, score));
		entities.add(new Ground(this.assets));
		entities.add(new City(this));

		views.add(new StartScene(this));
		views.add(new PlayScene(this));
		views.add(new SpriteBrowser(this));

		views.show(StartScene.class);
		Log.info("Birdy app initialized.");
	}

	private void readSpriteSheet() {
		BufferedImage spriteImage = assets.image("spritesheet/atlas.png");
		String[] spriteMap = assets.readTextFile("spritesheet/atlas.txt").split("\\s+");
		int scale = 1024;
		for (int i = 0; i < spriteMap.length;) {
			String spriteName = spriteMap[i++];
			i += 2; // skip next 2 tokens
			int x = Math.round(Float.parseFloat(spriteMap[i++]) * scale);
			int y = Math.round(Float.parseFloat(spriteMap[i++]) * scale);
			int width = Math.round(Float.parseFloat(spriteMap[i++]) * scale);
			int height = Math.round(Float.parseFloat(spriteMap[i++]) * scale);
			assets.storeImage(spriteName, spriteImage.getSubimage(x, y, width, height));
		}
	}
}