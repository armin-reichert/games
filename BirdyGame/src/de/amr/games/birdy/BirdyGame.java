package de.amr.games.birdy;

import java.awt.image.BufferedImage;

import de.amr.easy.game.Application;
import de.amr.easy.game.common.Score;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.birdy.assets.BirdySound;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.play.PlayScene;
import de.amr.games.birdy.scenes.start.StartScene;
import de.amr.games.birdy.scenes.util.SpriteBrowser;

public class BirdyGame extends Application {

	public static final BirdyGame Game = new BirdyGame();

	public static void main(String[] args) {
		Game.settings.fps = 60;
		Game.settings.title = "Zwick, das listige Vögelchen";
		Game.settings.width = 640;
		Game.settings.height = 480;
		Game.settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
		Game.settings.fullScreenOnStart = false;
		launch(Game);
	}

	public final Score score = new Score();

	@Override
	public void init() {
		readSpriteSheet();
		BirdySound.PLAYING_MUSIC.volume(-20);

		entities.add(new Bird(score));
		entities.add(new Ground());
		entities.add(new City());

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