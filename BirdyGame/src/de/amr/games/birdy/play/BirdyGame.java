package de.amr.games.birdy.play;

import java.awt.Font;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.play.scenes.IntroScene;
import de.amr.games.birdy.play.scenes.PlayScene;
import de.amr.games.birdy.play.scenes.StartScene;
import de.amr.games.birdy.utils.SpritesheetReader;

/**
 * "Flappy Bird"-like game.
 * 
 * @author Armin Reichert
 */
public class BirdyGame extends Application {

	public static void main(String[] args) {
		launch(new BirdyGame());
	}

	public BirdyGame() {
		// general settings
		settings.title = "Zwick, das listige Vögelchen";
		settings.width = 640;
		settings.height = 480;
		settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
		settings.fullScreenOnStart = false;

		// specific settings
		settings.set("jump key", KeyEvent.VK_UP);
		settings.set("world gravity", 0.4f);
		settings.set("world speed", -2.5f);
		settings.set("ready time sec", 2f);
		settings.set("max stars", 5);
		settings.set("bird flap millis", 50);
		settings.set("bird injured seconds", 1f);
		settings.set("min pipe creation sec", 1f);
		settings.set("max pipe creation sec", 5f);
		settings.set("pipe height", 480 - 112);
		settings.set("pipe width", 52);
		settings.set("min pipe height", 100);
		settings.set("passage height", 100);
	}

	@Override
	public void init() {
		SpritesheetReader.extractSpriteSheet(Assets.OBJECT);
		Assets.OBJECT.sound("music/bgmusic.mp3").volume(-20);
		Assets.OBJECT.storeFont("Pacifico-Regular", "fonts/Pacifico-Regular.ttf", 40, Font.BOLD);

		// create entities shared by different scenes:
		entities.add(new Bird(this));
		entities.add(new Ground(this));
		entities.add(new City(this));

		selectView(new IntroScene(this));
		addView(new StartScene(this));
		addView(new PlayScene(this));
	}
}