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

	public static void main(String[] args) {
		Settings.fps = 60;
		Settings.title = "Zwick, das listige Vögelchen";
		Settings.width = 640;
		Settings.height = 480;
		Settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
		Settings.fullScreenOnStart = false;
		launch(new BirdyGame());
	}

	public final Score score = new Score();

	@Override
	public void init() {
		readSpriteSheet();
		BirdySound.PLAYING_MUSIC.volume(-20);

		Entities.add(new Bird(score));
		Entities.add(new Ground());
		Entities.add(new City());

		Views.add(new StartScene(this));
		Views.add(new PlayScene(this));
		Views.add(new SpriteBrowser(this));

		Views.show(StartScene.class);
		Log.info("Birdy app initialized.");
	}

	private void readSpriteSheet() {
		BufferedImage spriteImage = Assets.image("spritesheet/atlas.png");
		String[] spriteMap = Assets.readTextFile("spritesheet/atlas.txt").split("\\s+");
		int scale = 1024;
		for (int i = 0; i < spriteMap.length;) {
			String spriteName = spriteMap[i++];
			i += 2; // skip next 2 tokens
			int x = Math.round(Float.parseFloat(spriteMap[i++]) * scale);
			int y = Math.round(Float.parseFloat(spriteMap[i++]) * scale);
			int width = Math.round(Float.parseFloat(spriteMap[i++]) * scale);
			int height = Math.round(Float.parseFloat(spriteMap[i++]) * scale);
			Assets.storeImage(spriteName, spriteImage.getSubimage(x, y, width, height));
		}
	}
}