package de.amr.games.birdy;

import java.awt.Font;
import java.awt.event.KeyEvent;

import de.amr.easy.game.Application;
import de.amr.easy.game.sprite.Sprite;
import de.amr.easy.game.ui.FullScreen;
import de.amr.games.birdy.entities.City;
import de.amr.games.birdy.entities.Ground;
import de.amr.games.birdy.entities.bird.Bird;
import de.amr.games.birdy.scenes.intro.IntroScene;
import de.amr.games.birdy.scenes.play.PlayScene;
import de.amr.games.birdy.scenes.start.StartScene;
import de.amr.games.birdy.scenes.util.SpriteBrowser;
import de.amr.games.birdy.tools.SpritesheetReader;

/**
 * "Flappy Bird"-like game.
 * 
 * @author Armin Reichert
 */
public class BirdyGame extends Application {

	public static void main(String[] args) {
		BirdyGame game = new BirdyGame();

		// general settings
		game.settings.title = "Zwick, das listige Vögelchen";
		game.settings.width = 640;
		game.settings.height = 480;
		game.settings.fullScreenMode = FullScreen.Mode(640, 480, 32);
		game.settings.fullScreenOnStart = false;

		// specific settings
		game.settings.set("jump key", KeyEvent.VK_UP);
		game.settings.set("world gravity", 0.4f);
		game.settings.set("world speed", -2.5f);
		game.settings.set("ready time sec", 2f);
		game.settings.set("max stars", 5);
		game.settings.set("bird flap millis", 50);
		game.settings.set("bird injured seconds", 1f);
		game.settings.set("min pipe creation sec", 1f);
		game.settings.set("max pipe creation sec", 5f);
		game.settings.set("pipe height", 480 - 112);
		game.settings.set("pipe width", 52);
		game.settings.set("min pipe height", 100);
		game.settings.set("passage height", 100);

		launch(game);
	}

	@Override
	public void init() {
		SpritesheetReader.extractSpriteSheet(assets);
		assets.sound("music/bgmusic.mp3").volume(-20);
		assets.storeFont("Pacifico-Regular", "fonts/Pacifico-Regular.ttf", 40, Font.BOLD);

		// create entities shared by different scenes:
		entities.add(new Bird(this));
		entities.add(new Ground(new Sprite(assets.image("land"))));
		entities.add(new City(this));

		views.add(new IntroScene(this));
		views.add(new StartScene(this));
		views.add(new PlayScene(this));
		views.add(new SpriteBrowser(this));

		views.show(IntroScene.class);
	}
}